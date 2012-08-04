#include "stdafx.h"

#include <sql.h>
#include <sqlext.h>

#include "odbcfunc.h"

#include <stdio.h>

__declspec(dllexport) double __stdcall TrapLots() {
	return 0.04;
}

__declspec(dllexport) int __stdcall test() {
	SQLHENV hEnv;
	DBConnectInit(&hEnv);

	SQLHDBC hDBC;
	if (!DBConnectDataSource("fxc", "fxc_dev", "fxc_dev", hEnv, &hDBC)) {
		return 1;
	}

	DBDisconnectDataSource(hEnv, hDBC);
	DBTerminate(hEnv);
	return 0;
}

