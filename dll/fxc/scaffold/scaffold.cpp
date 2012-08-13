// scaffold.cpp : コンソール アプリケーションのエントリ ポイントを定義します。
//

#include <stdio.h>
#include "stdafx.h"


extern int __stdcall InitEnv();
extern int __stdcall TerminateEnv();
extern int __stdcall GetTrapList(double *buffer);
extern int __stdcall UpdatePrice(double price);
extern int __stdcall GetTrapLots();
extern int __stdcall SetLongPosition(double *buffer, int *lots);
extern int __stdcall UpdateShortPosition(double *position);

int _tmain(int argc, _TCHAR* argv[])
{

	InitEnv();

	int lots = GetTrapLots();
	printf("LOTS=%d", lots);

	TerminateEnv();
	return 0;
}

