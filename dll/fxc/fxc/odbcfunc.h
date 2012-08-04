// ODBCFunc.h
#ifndef __ODBCFUNC__H
#define __ODBCFUNC__H

#ifdef __cplusplus
extern "C" {
#endif	// __cplusplus

//==========   prototype   ==========//

BOOL DBConnectInit(
	SQLHENV	*phEnv );

BOOL DBConnectDataSource(
	char	*szDsn,			
	char	*szUserName,	
	char	*szPassword,	
	SQLHENV	hEnv,			
	SQLHDBC	*phDbc );

BOOL DBEndTrans(
	SQLHENV		hEnv,
	SQLHDBC		hDbc,
	BOOL		bCommit );

BOOL DBDisconnectDataSource(
	SQLHENV		hEnv,
	SQLHDBC		hDbc );

BOOL DBTerminate(
	SQLHENV		hEnv );

BOOL DBExecute(
	SQLHENV		hEnv,
	SQLHDBC		hDbc, 
	SQLHSTMT	*phStmt, 
	char		*szCommandText, 
	BOOL		bOnlyPrepare );

BOOL DBCloseStmt(
	SQLHSTMT	hStmt );

#ifdef __cplusplus
}
#endif	// __cplusplus

#endif // __ODBCFUNC__H