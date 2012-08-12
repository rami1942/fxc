#include "stdafx.h"

#include <sql.h>
#include <sqlext.h>
#include <stdio.h>

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

__declspec(dllexport) int __stdcall UpdatePrice(double price) {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	char buf[1024];
	sprintf_s(buf, 1024, "update configuration set conf_value='%lf' where conf_key='current_price'", price);

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	DBDisconnectDataSource(hEnv, hDBC);
	return 1;
}

__declspec(dllexport) int __stdcall GetTrapLots() {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select conf_value from configuration where conf_key='lots'", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	long lots;
	SQLBindCol(hStmt, 1, SQL_C_LONG, &lots, 0, NULL);

	int i = 0;
	while(true) {
		int rc = SQLFetch(hStmt);
		if (rc == SQL_NO_DATA_FOUND) break;
		if (rc != SQL_SUCCESS && rc != SQL_SUCCESS_WITH_INFO) {
			DBCloseStmt(hStmt);
			DBDisconnectDataSource(hEnv, hDBC);
			return 0;
		}
	}
	DBCloseStmt(hStmt);

	DBDisconnectDataSource(hEnv, hDBC);
	return lots;
}
