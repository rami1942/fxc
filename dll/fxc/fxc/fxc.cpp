#include "stdafx.h"

#include <sql.h>
#include <sqlext.h>
#include <stdio.h>

#include "odbcfunc.h"

// SQL Injection? 聞こえないなぁ。マルチスレッド？それも聞こえないなぁ。

static SQLHENV hEnv;
static SQLHDBC hDBC;

//////////////////////////////////////////////////////////////////////////
// 環境、DBコネクション
//////////////////////////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////////////////////////
// Configから値を取得する
//////////////////////////////////////////////////////////////////////////

int GetConfigValue(const char *key, void *value, SQLSMALLINT colType) {
	char buf[1024];
	sprintf_s(buf, 1024, "select conf_value from configuration where conf_key='%s'", key);

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}

	SQLBindCol(hStmt, 1, colType, value, 0, NULL);

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
	return 1;
}

__declspec(dllexport) double __stdcall GetConfigByDouble(const char *key) {
	double val;
	if (GetConfigValue(key, &val, SQL_C_DOUBLE)) {
		return val;
	} else {
		return 0;
	}
}

__declspec(dllexport) int __stdcall GetConfigByInt(const char *key) {
	long val;
	if (GetConfigValue(key, &val, SQL_C_LONG)) {
		return val;
	} else {
		return 0;
	}
}

__declspec(dllexport) int __stdcall GetTrapLots() {
	return GetConfigByInt("lots");
}

__declspec(dllexport) double __stdcall GetTakeProfitWidth() {
	return GetConfigByDouble("tp_width");
}

//////////////////////////////////////////////////////////////////////////
//
//////////////////////////////////////////////////////////////////////////
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

__declspec(dllexport) int __stdcall GetHistoryRequest(int *ticketNo, int *posCd) {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select ticket_no, pos_cd from history_request", false)) {
		return 0;
	}

	long ticket;
	long pos;

	SQLBindCol(hStmt, 1, SQL_C_LONG, &ticket, 0, NULL);
	SQLBindCol(hStmt, 2, SQL_C_LONG, &pos, 0, NULL);

	int i = 0;
	while(true) {
		int rc = SQLFetch(hStmt);
		if (rc == SQL_NO_DATA_FOUND) break;
		if (rc != SQL_SUCCESS && rc != SQL_SUCCESS_WITH_INFO) {
			DBCloseStmt(hStmt);
			return 0;
		}
		ticketNo[i] = (int)ticket;
		posCd[i] = (int)pos;

		i++;
	}
	DBCloseStmt(hStmt);
	ticketNo[i] = 0;

	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from history_request", false)) {
		DBEndTrans(hEnv, hDBC, false);
		return 0;
	}
	DBEndTrans(hEnv, hDBC, true);
	return 1;
}

__declspec(dllexport) int __stdcall GetToggleTpRequest(int *ticketNo, double *tpPrice) {
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, "select ticket_no, tp_price from toggle_tp_request", false)) {
		return 0;
	}

	long ticket;
	double tp;
	SQLBindCol(hStmt, 1, SQL_C_LONG, &ticket, 0, NULL);
	SQLBindCol(hStmt, 2, SQL_C_DOUBLE, &tp, 0, NULL);

	int i = 0;
	while(true) {
		int rc = SQLFetch(hStmt);
		if (rc == SQL_NO_DATA_FOUND) break;
		if (rc != SQL_SUCCESS && rc != SQL_SUCCESS_WITH_INFO) {
			DBCloseStmt(hStmt);
			return 0;
		}
		ticketNo[i] = (int)ticket;
		tpPrice[i] = tp;
		i++;
	}

	DBCloseStmt(hStmt);
	ticketNo[i] = 0;

	if (!DBExecute(hEnv, hDBC, &hStmt, "delete from toggle_tp_request", false)) {
		DBEndTrans(hEnv, hDBC, false);
		return 0;
	}
	DBEndTrans(hEnv, hDBC, true);
	return 1;

}

//////////////////////////////////////////////////////////////////////////
// 
//////////////////////////////////////////////////////////////////////////

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

//////////////////////////////////////////////////////////////////////////
// 
//////////////////////////////////////////////////////////////////////////

__declspec(dllexport) int __stdcall UpdatePosition(int ticket_no, int magic_no, int pos_type, double open_price, double take_profit, double stop_loss, int swap, double profit, double lots, char *symbol) {
	char buf[2048];
	sprintf_s(buf, 2048, "insert into position (ticket_no, magic_no, pos_type, open_price, tp_price, sl_price, swap_point, profit, lots, symbol) " 
		"values (%d, %d, %d, %lf, %lf, %lf, %d, %lf, %lf, '%s') on duplicate key update is_real=0,tp_price=%lf,sl_price=%lf,swap_point=%d,profit=%lf",
			ticket_no, magic_no, pos_type, open_price, take_profit, stop_loss, swap, profit, lots, symbol, take_profit, stop_loss, swap, profit
		);
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}
	return 1;
}

__declspec(dllexport) int __stdcall InsertHistory(
	int ticket_no, int magic_no, int pos_type, int pos_cd, char *open_dt, double lots, char *symbol,
	double open_price, double sl_price, double tp_price, char *close_dt, double close_price, double swap_point, double profit) {
	char buf[2048];

	sprintf_s(buf, 2048, "insert into position_history (ticket_no, magic_no, pos_type, open_dt, pos_cd, lots, symbol, open_price, sl_price, tp_price, close_dt, close_price, swap_point, profit) "
		"values (%d, %d, %d, '%s', %d, %lf, '%s', %lf, %lf, %lf, '%s', %lf, %lf, %lf)",
		ticket_no, magic_no, pos_type, open_dt, pos_cd, lots, symbol, open_price, sl_price, tp_price, close_dt, close_price, swap_point, profit);

	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}
	return 1;
}

//////////////////////////////////////////////////////////////////////////
// config更新関連
//////////////////////////////////////////////////////////////////////////

__declspec(dllexport) int __stdcall SetConfigDouble(const char* key, double value) {
	char buf[1024];
	sprintf_s(buf, 1024, "update configuration set conf_value=%lf where conf_key='%s'", value, key);
	SQLHSTMT hStmt;
	if (!DBExecute(hEnv, hDBC, &hStmt, buf, false)) {
		return 0;
	}
	return 1;
}

//////////////////////////////////////////////////////////////////////////
// Obsolete
//////////////////////////////////////////////////////////////////////////
