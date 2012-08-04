// scaffold.cpp : コンソール アプリケーションのエントリ ポイントを定義します。
//

#include <stdio.h>
#include "stdafx.h"


extern int __stdcall InitEnv();
extern int __stdcall TerminateEnv();
extern int __stdcall GetTrapList(double *buffer);


int _tmain(int argc, _TCHAR* argv[])
{

	InitEnv();

	double buf[64];
	if (!GetTrapList(buf)) {
		printf("GetTrapList fail.\n");
		TerminateEnv();
		return 0;
	}

	int i = 0;
	while (true) {
		if (buf[i] == 0.0L) break;
		printf("%lf\n", buf[i]);
		i++;
	}

	TerminateEnv();
	return 0;
}

