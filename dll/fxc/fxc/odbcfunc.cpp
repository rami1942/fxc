// from http://frog.raindrop.jp/knowledge/archives/000604.html

// ODBCFunc.cpp
#define WIN32_LEAN_AND_MEAN
#include "stdafx.h"
#include <windows.h>		// sqltypes.h 内でHWNDのtypedefがあるため
#include <sql.h>
#include <sqlext.h>
#include <sqltypes.h>
#include <stdio.h>
#include <errno.h>

#include "ODBCFunc.h"

extern "C" {
//==========   define   ==========//
#define	STATESIZE		50				// SQLSTATEのサイズ
#define	BUFSIZE			1024			// メッセージバッファのサイズ

//==========   global   ==========//
__declspec(thread)	int		tls_nLastErrNo				= 0;	// 最後のODBCエラーコード
__declspec(thread)	char	tls_szLastErrMsg[BUFSIZE+1]	= "";	// 最後のODBCエラーメッセージ

//==========   macro / inline   ==========//

// ODBC関数の成否を判断する
inline	BOOL	SQLIsSuccess( SQLRETURN Ret ) { return ( ( SQL_SUCCESS == Ret ) || ( SQL_SUCCESS_WITH_INFO == Ret ) ); };

// フェッチの結果より、カーソル終端を判断する
inline	BOOL	SQLIsEOF( SQLRETURN Ret ) { return ( SQL_NO_DATA == Ret ); }

// ODBC関数のエラーを設定する
inline	BOOL	SQLSetLastError( 
	SQLSMALLINT	nHandleType,	// (in)	ハンドルタイプ
	SQLHANDLE	hSQL )			// (in)	ハンドル
{ 
	char		szState[STATESIZE+1]	= "";		// SQLSTATE取得用
	SQLSMALLINT	nRetLength				= 0;		// エラーメッセージ長

	return SQLIsSuccess( SQLGetDiagRec( nHandleType, hSQL, 1, (SQLCHAR*)szState, 
		(SQLINTEGER*)&tls_nLastErrNo, (SQLCHAR*)tls_szLastErrMsg, BUFSIZE, &nRetLength ) );
}

// 初期化処理
BOOL DBConnectInit(
	SQLHENV	*phEnv )		// (out)確保したENVハンドル
{
	SQLRETURN	Ret;		// 戻り値取得用



	//...Debug----->
	printf( "[DBConnectInit] Start" );

	// 環境ハンドルのアロケート
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_ENV, SQL_NULL_HANDLE, (SQLHANDLE*)phEnv ) ) )
	{
		SQLSetLastError( SQL_HANDLE_ENV, *phEnv );
		printf( "[DBConnectInit]   SQLAllocHandle(ENV) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else
	{
		// 環境属性の設定(ODBC 3.0)
		if ( !SQLIsSuccess( Ret = SQLSetEnvAttr( *phEnv, SQL_ATTR_ODBC_VERSION, (void*)SQL_OV_ODBC3, 0 ) ) )	
		{
			SQLSetLastError( SQL_HANDLE_ENV, *phEnv );
			printf( "[DBConnectInit]   SQLSetEnvAttr(ODBC 3.0) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
		}
		else 
		{
			//...Debug----->
			printf( "[DBConnectInit] Succeed" );
			return TRUE;
// ---------------------------------> 以降は異常発生時
		}

		// 環境ハンドルの解放
		SQLFreeHandle( SQL_HANDLE_ENV, (SQLHANDLE)*phEnv );
	}

	//...Debug----->
	printf( "[DBConnectInit] Failed" );
	return FALSE;
}
	
// データソースに接続
BOOL DBConnectDataSource(
	char	*szDsn,			// (in)	データソース名
	char	*szUserName,	// (in)	ユーザ名
	char	*szPassword,	// (in)	パスワード
	SQLHENV	hEnv,			// (out)ENVハンドル
	SQLHDBC	*phDbc )		// (out)接続ハンドルのポインタ
{
	SQLRETURN	Ret;		// 戻り値取得用



	//...Debug----->
	printf( "[DBConnectDataSource] Start" );
		
	// 接続ハンドルのアロケート
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_DBC, hEnv, (SQLHANDLE*)phDbc ) ) )
	{
		SQLSetLastError( SQL_HANDLE_ENV, hEnv );
		printf( "[DBConnectDataSource]   SQLAllocHandle(DBC) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else
	{
		// 接続する
		if ( !SQLIsSuccess( Ret = SQLConnect( *phDbc, 
											(SQLCHAR *)szDsn,		SQL_NTS, 
											(SQLCHAR *)szUserName,	SQL_NTS, 
											(SQLCHAR *)szPassword,	SQL_NTS ) ) )
		{
			SQLSetLastError( SQL_HANDLE_DBC, *phDbc );
			printf( "[DBConnectDataSource]   SQLConnect failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
			char buf[1024];
			WideCharToMultiByte(CP_ACP, 0, (LPCWSTR)tls_szLastErrMsg, -1, buf, 1024, NULL, NULL);
			printf("%s\n", buf);
			Sleep(1);
		}

		else
		{
			// 手動コミットに変更する
			// →ドキュメントにはSQLConnectの前でも後でもかまわないとなっているが、後でないとコミットできない??
			if ( !SQLIsSuccess( Ret = SQLSetConnectAttr( *phDbc, SQL_ATTR_AUTOCOMMIT, (SQLPOINTER)SQL_AUTOCOMMIT_OFF, SQL_NTS ) ) )
			{
				SQLSetLastError( SQL_HANDLE_DBC, *phDbc );
				printf( "[DBConnectDataSource]   SQLSetConnectAttr(SQL_ATTR_AUTOCOMMIT) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
			}
			else 
			{
				//...Debug----->
				printf( "[DBConnectDataSource] Succeed" );
				return TRUE;
// ---------------------------------> 以降は異常発生時
			}
			// 切断
			SQLDisconnect( *phDbc );
		}

		// DBハンドルの解放
		SQLFreeHandle( SQL_HANDLE_DBC, (SQLHANDLE)*phDbc );
	}

	//...Debug----->
	printf( "[DBConnectDataSource] Failed" );
	return FALSE;
}

// トランザクションの完了
BOOL DBEndTrans(
	SQLHENV		hEnv,		// (in)	ENVハンドル
	SQLHDBC		hDbc,		// (in)	接続ハンドル
	BOOL		bCommit )	// (in)	TRUEならコミット/FALSEならロールバック
{
	SQLRETURN	Ret;				// 戻り値取得用
	SQLSMALLINT	siCompletion = 0;	// Commit Or Rollback	




	//...Debug----->
	printf( "[DBEndTrans] Start" );
	
	// Commit Or Rollback
	if ( bCommit )
	{
		siCompletion = SQL_COMMIT;
		//...Debug----->
		printf( "[DBEndTrans]   Begin exec Commit..." );
	}
	else 
	{
		siCompletion = SQL_ROLLBACK;
		//...Debug----->
		printf( "[DBEndTrans]   Begin exec Rollback..." );
	}
	
	// トランザクション終了
	if ( !SQLIsSuccess( Ret = SQLEndTran( SQL_HANDLE_ENV, (SQLHANDLE)hEnv, siCompletion ) ) )
	{
		SQLSetLastError( SQL_HANDLE_ENV, hEnv );
		printf( "[DBEndTrans]   SQLEndTran failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else 
	{
		//...Debug----->
		printf( "[DBEndTrans] Succeed" );
		return TRUE;
// ---------------------------------> 以降は異常発生時
	}

	//...Debug----->
	printf( "[DBEndTrans] Failed" );
	return FALSE;
}


// データソース切断処理
BOOL DBDisconnectDataSource(
	SQLHENV		hEnv,		// (in)	ENVハンドル
	SQLHDBC		hDbc )		// (in)	接続ハンドル
{
	
	//...Debug----->
	printf( "[DBDisconnectDataSource] Start" );

	// 接続を切断し、ハンドルを解放する
	SQLDisconnect( hDbc );								// 切断
	SQLFreeHandle( SQL_HANDLE_DBC, (SQLHANDLE)hDbc );	// 接続ハンドル解放
	
	//...Debug----->
	printf( "[DBDisconnectDataSource] Succeed" );
	return TRUE;
}

// 終業処理
BOOL DBTerminate(
	SQLHENV		hEnv )		// (in)	ENVハンドル
{
	//...Debug----->
	printf( "[DBTerminate] Start" );

	// ハンドルを解放する
	SQLFreeHandle( SQL_HANDLE_ENV, (SQLHANDLE)hEnv );	// 環境ハンドル解放
	
	//...Debug----->
	printf( "[DBTerminate] Succeed" );
	return TRUE;
}


// SQL実行処理
BOOL DBExecute(
	SQLHENV		hEnv,					// (in)	ENVハンドル
	SQLHDBC		hDbc, 					// (in)	接続ハンドル
	SQLHSTMT	*phStmt, 				// (out)実行ハンドル
	char		*szCommandText,			// (in)	実行ステートメント
	BOOL		bOnlyPrepare = FALSE )	// (in)	TRUEなら実行はせず、解析のみ
{
	SQLRETURN	Ret;	// 戻り値取得用



	//...Debug----->
	printf( "[DBExecute] Start" );
	
	// 実行ハンドルのアロケート
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_STMT, (SQLHANDLE)hDbc, (SQLHANDLE *)phStmt ) ) )
	{
		SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
		printf( "[DBExecute]   SQLAllocHandle(STMT) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else 
	{
		// SQLを解析する
		if ( !SQLIsSuccess( Ret = SQLPrepare( *phStmt, (SQLCHAR *)szCommandText, SQL_NTS ) ) ) 
		{
			SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
			printf( "[DBExecute]   SQLPrepare failed -->(%d:%s) SQL = [%s]", tls_nLastErrNo, tls_szLastErrMsg, szCommandText );
		}
		// 解析のみなら完了
		else if ( bOnlyPrepare )
		{
			//...Debug----->
			printf( "[DBExecute] Succeed" );
			return TRUE;
		}
		// 実行する
		else if ( !SQLIsSuccess( Ret = SQLExecute( *phStmt ) ) )
		{
			SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
			printf( "[DBExecute]   SQLExecute failed -->(%d:%s) SQL = [%s]", tls_nLastErrNo, tls_szLastErrMsg, szCommandText );
		}
		// 完了
		else
		{
			//...Debug----->
			printf( "[DBExecute] Succeed" );
			return TRUE;
// ---------------------------------> 以降は異常発生時
		}
		SQLFreeHandle( SQL_HANDLE_STMT, (SQLHANDLE)*phStmt );
	}

	//...Debug----->
	printf( "[DBExecute] Failed" );
	return FALSE;
}


// 実行ハンドルのクローズ
BOOL DBCloseStmt(
	SQLHSTMT	hStmt )		// (in)	実行ハンドル
{
	//...Debug----->
	printf( "[DBCloseStmt] Start" );

	SQLFreeHandle( SQL_HANDLE_STMT, (SQLHANDLE)hStmt );

	//...Debug----->
	printf( "[DBCloseStmt] End" );
	return TRUE;
}

} // extern "C"