// scaffold.cpp : コンソール アプリケーションのエントリ ポイントを定義します。
//

#include <stdio.h>
#include "stdafx.h"


extern int __stdcall InitEnv();
extern int __stdcall TerminateEnv();
extern int __stdcall Connect();
extern int __stdcall Disconnect();
extern int __stdcall GetTrapList(double *buffer);
extern int __stdcall UpdatePrice(double price);
extern int __stdcall GetTrapLots();
extern int __stdcall SetLongPosition(double *buffer, int *lots);
extern int __stdcall UpdateShortTrap(double *position);
extern int __stdcall UpdateLongPosition(double *position, int *lots);
extern int __stdcall SetMark();
extern int __stdcall ClearMark();
extern int __stdcall UpdatePosition(int ticket_no, int magic_no, int pos_type, double open_price, double take_profit, double stop_loss, int swap);

int _tmain(int argc, _TCHAR* argv[])
{

	InitEnv();

	/*
	double price[] = {83.105, 83.058, 82.735, 82.561, 0};
	int lots[] = {21000, 14000, 21000, 14000, 0};
	Connect();
	UpdateLongPosition(price, lots);
	Disconnect();
	*/

	Connect();
	SetMark();
	UpdatePosition(10, 10, 1, 80.0, 80.0, 80.0, 10);
	ClearMark();
	Disconnect();

	TerminateEnv();
	return 0;
}

