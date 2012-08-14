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

__declspec(dllexport) double __stdcall GetTakeProfitWidth() {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select conf_value from configuration where conf_key='tp_width'", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	double tpWidth;
	SQLBindCol(hStmt, 1, SQL_C_DOUBLE, &tpWidth, 0, NULL);

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
	return tpWidth;
}

__declspec(dllexport) int __stdcall SetLongPosition(double *buffer, int *lots) {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from long_position", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}
	int i = 0;
	while(true) {
		if (buffer[i] == 0) break;

		char buf[1024];
		sprintf_s(buf, 1024, "insert into long_position (open_price, lots) values (%lf, %d)", buffer[i], lots[i]);

		if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
			DBEndTrans(hEnv, hDBC, false);
			DBDisconnectDataSource(hEnv, hDBC);
			return 0;
		}

		i++;
	}

	DBEndTrans(hEnv, hDBC, true);
	DBDisconnectDataSource(hEnv, hDBC);
	return 1;
}

__declspec(dllexport) int __stdcall UpdateShortPosition(double *position) {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "update short_position set is_real = 2 where is_real=1", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}
	int i = 0;
	while(true) {
		if (position[i] == 0) break;

		char buf[1024];
		sprintf_s(buf, 1024, "update short_position set is_real = 1 where open_price = %lf", position[i]);

		if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
			DBEndTrans(hEnv, hDBC, false);
			DBDisconnectDataSource(hEnv, hDBC);
			return 0;
		}

		i++;
	}

	if (!DBExecute(hEnv, hDBC, &hStmt, "update short_position set is_real = 0 where is_real=2", false)) {
		DBEndTrans(hEnv, hDBC, false);
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	DBDisconnectDataSource(hEnv, hDBC);
	return 1;
}

__declspec(dllexport) int __stdcall UpdateLongPosition(double *position, int *lots) {
	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "update long_position set is_real = 0", false)) {
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	int i = 0;
	while (true) {
		if (position[i] == 0.0) break;

		char buf[1024];
		sprintf_s(buf, 1024, "insert into long_position (open_price, lots, is_real) values (%lf, %d, 1) on duplicate key update is_real=1", position[i], lots[i]);

		if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
			DBEndTrans(hEnv, hDBC, false);
			DBDisconnectDataSource(hEnv, hDBC);
			return 0;
		}

		i++;
	}
	
	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from long_position where is_real=0", false)) {
		DBEndTrans(hEnv, hDBC, false);
		DBDisconnectDataSource(hEnv, hDBC);
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	DBDisconnectDataSource(hEnv, hDBC);
	return 1;
}
