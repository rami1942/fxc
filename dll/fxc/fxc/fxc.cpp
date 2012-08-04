#include "stdafx.h"

#include <sql.h>
#include <sqlext.h>

#include "odbcfunc.h"

static SQLHENV hEnv;

__declspec(dllexport) int __stdcall InitEnv() {
	DBConnectInit(&hEnv);
	return 1;
}

__declspec(dllexport) int __stdcall TerminateEnv() {
	DBTerminate(hEnv);
	return 1;
}

__declspec(dllexport) int __stdcall GetTrapList(double *buffer) {

	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select open_price from short_position order by open_price desc", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	double price;
	SQLBindCol(hStmt, 1, SQL_C_DOUBLE, &price, 0, NULL);

	int i = 0;
	while(true) {
		int rc = SQLFetch(hStmt);
		if (rc == SQL_NO_DATA_FOUND) break;
		if (rc != SQL_SUCCESS && rc != SQL_SUCCESS_WITH_INFO) {
			DBCloseStmt(hStmt);
			DBDisconnectDataSource(hEnv, hDBC);
			return 0;
		}
		buffer[i++] = price;
	}
	DBCloseStmt(hStmt);
	buffer[i++] = 0.0L;

	DBDisconnectDataSource(hEnv, hDBC);
	return 1;
}
