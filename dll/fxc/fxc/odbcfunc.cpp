// from http://frog.raindrop.jp/knowledge/archives/000604.html

// ODBCFunc.cpp
#define WIN32_LEAN_AND_MEAN
#include "stdafx.h"
#include <windows.h>		// sqltypes.h ����HWND��typedef�����邽��
#include <sql.h>
#include <sqlext.h>
#include <sqltypes.h>
#include <stdio.h>
#include <errno.h>

#include "ODBCFunc.h"

extern "C" {
//==========   define   ==========//
#define	STATESIZE		50				// SQLSTATE�̃T�C�Y
#define	BUFSIZE			1024			// ���b�Z�[�W�o�b�t�@�̃T�C�Y

//==========   global   ==========//
__declspec(thread)	int		tls_nLastErrNo				= 0;	// �Ō��ODBC�G���[�R�[�h
__declspec(thread)	char	tls_szLastErrMsg[BUFSIZE+1]	= "";	// �Ō��ODBC�G���[���b�Z�[�W

//==========   macro / inline   ==========//

// ODBC�֐��̐��ۂ𔻒f����
inline	BOOL	SQLIsSuccess( SQLRETURN Ret ) { return ( ( SQL_SUCCESS == Ret ) || ( SQL_SUCCESS_WITH_INFO == Ret ) ); };

// �t�F�b�`�̌��ʂ��A�J�[�\���I�[�𔻒f����
inline	BOOL	SQLIsEOF( SQLRETURN Ret ) { return ( SQL_NO_DATA == Ret ); }

// ODBC�֐��̃G���[��ݒ肷��
inline	BOOL	SQLSetLastError( 
	SQLSMALLINT	nHandleType,	// (in)	�n���h���^�C�v
	SQLHANDLE	hSQL )			// (in)	�n���h��
{ 
	char		szState[STATESIZE+1]	= "";		// SQLSTATE�擾�p
	SQLSMALLINT	nRetLength				= 0;		// �G���[���b�Z�[�W��

	return SQLIsSuccess( SQLGetDiagRec( nHandleType, hSQL, 1, (SQLCHAR*)szState, 
		(SQLINTEGER*)&tls_nLastErrNo, (SQLCHAR*)tls_szLastErrMsg, BUFSIZE, &nRetLength ) );
}

// ����������
BOOL DBConnectInit(
	SQLHENV	*phEnv )		// (out)�m�ۂ���ENV�n���h��
{
	SQLRETURN	Ret;		// �߂�l�擾�p



	//...Debug----->
	printf( "[DBConnectInit] Start" );

	// ���n���h���̃A���P�[�g
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_ENV, SQL_NULL_HANDLE, (SQLHANDLE*)phEnv ) ) )
	{
		SQLSetLastError( SQL_HANDLE_ENV, *phEnv );
		printf( "[DBConnectInit]   SQLAllocHandle(ENV) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else
	{
		// �������̐ݒ�(ODBC 3.0)
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
// ---------------------------------> �ȍ~�ُ͈픭����
		}

		// ���n���h���̉��
		SQLFreeHandle( SQL_HANDLE_ENV, (SQLHANDLE)*phEnv );
	}

	//...Debug----->
	printf( "[DBConnectInit] Failed" );
	return FALSE;
}
	
// �f�[�^�\�[�X�ɐڑ�
BOOL DBConnectDataSource(
	char	*szDsn,			// (in)	�f�[�^�\�[�X��
	char	*szUserName,	// (in)	���[�U��
	char	*szPassword,	// (in)	�p�X���[�h
	SQLHENV	hEnv,			// (out)ENV�n���h��
	SQLHDBC	*phDbc )		// (out)�ڑ��n���h���̃|�C���^
{
	SQLRETURN	Ret;		// �߂�l�擾�p



	//...Debug----->
	printf( "[DBConnectDataSource] Start" );
		
	// �ڑ��n���h���̃A���P�[�g
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_DBC, hEnv, (SQLHANDLE*)phDbc ) ) )
	{
		SQLSetLastError( SQL_HANDLE_ENV, hEnv );
		printf( "[DBConnectDataSource]   SQLAllocHandle(DBC) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else
	{
		// �ڑ�����
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
			// �蓮�R�~�b�g�ɕύX����
			// ���h�L�������g�ɂ�SQLConnect�̑O�ł���ł����܂�Ȃ��ƂȂ��Ă��邪�A��łȂ��ƃR�~�b�g�ł��Ȃ�??
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
// ---------------------------------> �ȍ~�ُ͈픭����
			}
			// �ؒf
			SQLDisconnect( *phDbc );
		}

		// DB�n���h���̉��
		SQLFreeHandle( SQL_HANDLE_DBC, (SQLHANDLE)*phDbc );
	}

	//...Debug----->
	printf( "[DBConnectDataSource] Failed" );
	return FALSE;
}

// �g�����U�N�V�����̊���
BOOL DBEndTrans(
	SQLHENV		hEnv,		// (in)	ENV�n���h��
	SQLHDBC		hDbc,		// (in)	�ڑ��n���h��
	BOOL		bCommit )	// (in)	TRUE�Ȃ�R�~�b�g/FALSE�Ȃ烍�[���o�b�N
{
	SQLRETURN	Ret;				// �߂�l�擾�p
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
	
	// �g�����U�N�V�����I��
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
// ---------------------------------> �ȍ~�ُ͈픭����
	}

	//...Debug----->
	printf( "[DBEndTrans] Failed" );
	return FALSE;
}


// �f�[�^�\�[�X�ؒf����
BOOL DBDisconnectDataSource(
	SQLHENV		hEnv,		// (in)	ENV�n���h��
	SQLHDBC		hDbc )		// (in)	�ڑ��n���h��
{
	
	//...Debug----->
	printf( "[DBDisconnectDataSource] Start" );

	// �ڑ���ؒf���A�n���h�����������
	SQLDisconnect( hDbc );								// �ؒf
	SQLFreeHandle( SQL_HANDLE_DBC, (SQLHANDLE)hDbc );	// �ڑ��n���h�����
	
	//...Debug----->
	printf( "[DBDisconnectDataSource] Succeed" );
	return TRUE;
}

// �I�Ə���
BOOL DBTerminate(
	SQLHENV		hEnv )		// (in)	ENV�n���h��
{
	//...Debug----->
	printf( "[DBTerminate] Start" );

	// �n���h�����������
	SQLFreeHandle( SQL_HANDLE_ENV, (SQLHANDLE)hEnv );	// ���n���h�����
	
	//...Debug----->
	printf( "[DBTerminate] Succeed" );
	return TRUE;
}


// SQL���s����
BOOL DBExecute(
	SQLHENV		hEnv,					// (in)	ENV�n���h��
	SQLHDBC		hDbc, 					// (in)	�ڑ��n���h��
	SQLHSTMT	*phStmt, 				// (out)���s�n���h��
	char		*szCommandText,			// (in)	���s�X�e�[�g�����g
	BOOL		bOnlyPrepare = FALSE )	// (in)	TRUE�Ȃ���s�͂����A��͂̂�
{
	SQLRETURN	Ret;	// �߂�l�擾�p



	//...Debug----->
	printf( "[DBExecute] Start" );
	
	// ���s�n���h���̃A���P�[�g
	if ( !SQLIsSuccess( Ret = SQLAllocHandle( SQL_HANDLE_STMT, (SQLHANDLE)hDbc, (SQLHANDLE *)phStmt ) ) )
	{
		SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
		printf( "[DBExecute]   SQLAllocHandle(STMT) failed -->(%d:%s)", tls_nLastErrNo, tls_szLastErrMsg );
	}
	else 
	{
		// SQL����͂���
		if ( !SQLIsSuccess( Ret = SQLPrepare( *phStmt, (SQLCHAR *)szCommandText, SQL_NTS ) ) ) 
		{
			SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
			printf( "[DBExecute]   SQLPrepare failed -->(%d:%s) SQL = [%s]", tls_nLastErrNo, tls_szLastErrMsg, szCommandText );
		}
		// ��݂͂̂Ȃ犮��
		else if ( bOnlyPrepare )
		{
			//...Debug----->
			printf( "[DBExecute] Succeed" );
			return TRUE;
		}
		// ���s����
		else if ( !SQLIsSuccess( Ret = SQLExecute( *phStmt ) ) )
		{
			SQLSetLastError( SQL_HANDLE_STMT, *phStmt );
			printf( "[DBExecute]   SQLExecute failed -->(%d:%s) SQL = [%s]", tls_nLastErrNo, tls_szLastErrMsg, szCommandText );
		}
		// ����
		else
		{
			//...Debug----->
			printf( "[DBExecute] Succeed" );
			return TRUE;
// ---------------------------------> �ȍ~�ُ͈픭����
		}
		SQLFreeHandle( SQL_HANDLE_STMT, (SQLHANDLE)*phStmt );
	}

	//...Debug----->
	printf( "[DBExecute] Failed" );
	return FALSE;
}


// ���s�n���h���̃N���[�Y
BOOL DBCloseStmt(
	SQLHSTMT	hStmt )		// (in)	���s�n���h��
{
	//...Debug----->
	printf( "[DBCloseStmt] Start" );

	SQLFreeHandle( SQL_HANDLE_STMT, (SQLHANDLE)hStmt );

	//...Debug----->
	printf( "[DBCloseStmt] End" );
	return TRUE;
}

} // extern "C"