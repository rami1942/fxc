// scaffold.cpp : コンソール アプリケーションのエントリ ポイントを定義します。
//

#include <stdio.h>
#include "stdafx.h"


extern int __stdcall InitEnv();
extern int __stdcall TerminateEnv();
extern int __stdcall GetTrapList(double *buffer);
extern int __stdcall UpdatePrice(double price);
extern int __stdcall GetTrapLots();

int _tmain(int argc, _TCHAR* argv[])
{

	InitEnv();

	int n = GetTrapLots();

	TerminateEnv();
	return 0;
}

