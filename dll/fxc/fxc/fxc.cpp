#include "stdafx.h"

#include <sql.h>
#include <sqlext.h>
#include <stdio.h>

#include "odbcfunc.h"

static SQLHENV hEnv;
static SQLHDBC hDBC;

__declspec(dllexport) int __stdcall InitEnv() {
	DBConnectInit(&hEnv);
	return 1;
}

__declspec(dllexport) int __stdcall TerminateEnv() {
	DBTerminate(hEnv);
	return 1;
}

__declspec(dllexport) int __stdcall Connect() {
	if (!DBConnectDataSource("fxc", "", "", hEnv, &hDBC)) return 0;
	return 1;
}

__declspec(dllexport) int __stdcall Disconnect() {
	DBDisconnectDataSource(hEnv, hDBC);
	return 0;
}

__declspec(dllexport) int __stdcall GetTrapList(double *buffer) {

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select open_price from short_trap order by open_price desc", false)) {
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
			return 0;
		}
		buffer[i++] = price;
	}
	DBCloseStmt(hStmt);
	buffer[i++] = 0.0L;

	return 1;
}

__declspec(dllexport) int __stdcall UpdatePrice(double price) {
	char buf[1024];
	sprintf_s(buf, 1024, "update configuration set conf_value='%lf' where conf_key='current_price'", price);

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	return 1;
}

__declspec(dllexport) int __stdcall GetTrapLots() {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select conf_value from configuration where conf_key='lots'", false)) {
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
			return 0;
		}
	}
	DBCloseStmt(hStmt);
	return lots;
}

__declspec(dllexport) double __stdcall GetTakeProfitWidth() {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select conf_value from configuration where conf_key='tp_width'", false)) {
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
			return 0;
		}
	}
	DBCloseStmt(hStmt);
	return tpWidth;
}

__declspec(dllexport) int __stdcall UpdateShortTrap(double *position) {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "update short_trap set is_real = 2 where is_real=1", false)) {
		return 0;
	}
	int i = 0;
	while(true) {
		if (position[i] == 0) break;

		char buf[1024];
		sprintf_s(buf, 1024, "update short_trap set is_real = 1 where open_price = %lf", position[i]);

		if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
			DBEndTrans(hEnv, hDBC, false);
			return 0;
		}

		i++;
	}

	if (!DBExecute(hEnv, hDBC, &hStmt, "update short_trap set is_real = 0 where is_real=2", false)) {
		DBEndTrans(hEnv, hDBC, false);
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	return 1;
}

__declspec(dllexport) int __stdcall UpdateLongPosition(double *position, int *lots) {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "update long_position set is_real = 0", false)) {
		return 0;
	}

	int i = 0;
	while (true) {
		if (position[i] == 0.0) break;

		char buf[1024];
		sprintf_s(buf, 1024, "insert into long_position (open_price, lots, is_real) values (%lf, %d, 1) on duplicate key update is_real=1", position[i], lots[i]);

		if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
			DBEndTrans(hEnv, hDBC, false);
			return 0;
		}

		i++;
	}
	
	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from long_position where is_real=0", false)) {
		DBEndTrans(hEnv, hDBC, false);
		return 0;
	}

	DBEndTrans(hEnv, hDBC, true);
	return 1;
}

__declspec(dllexport) int __stdcall GetDeleteRequest(double *position) {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select price from delete_request", false)) {
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
			return 0;
		}
		position[i] = price;

		i++;
	}
	DBCloseStmt(hStmt);
	position[i] = 0.0;

	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from delete_request", false)) {
		DBEndTrans(hEnv, hDBC, false);
		return 0;
	}


	DBEndTrans(hEnv, hDBC, true);
	return 1;
}

__declspec(dllexport) int __stdcall SetMark() {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "update position set is_real = 1", false)) {
		return 0;
	}
	return 1;
}

__declspec(dllexport) int __stdcall ClearMark() {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from position where is_real = 1", false)) {
		return 0;
	}
	return 1;
}

__declspec(dllexport) int __stdcall UpdatePosition(int ticket_no, int magic_no, int pos_type, double open_price, double take_profit, double stop_loss, int swap) {
	char buf[2048];
	sprintf_s(buf, 2048, "insert into position (ticket_no, magic_no, pos_type, open_price, tp_price, sl_price, swap_point) " 
		"values (%d, %d, %d, %lf, %lf, %lf, %d) on duplicate key update is_real=0,tp_price=%lf,sl_price=%lf,swap_point=%d",
			ticket_no, magic_no, pos_type, open_price, take_profit, stop_loss, swap, take_profit, stop_loss, swap
		);
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}
	return 1;
}