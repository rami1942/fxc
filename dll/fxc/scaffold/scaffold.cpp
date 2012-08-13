// scaffold.cpp : �R���\�[�� �A�v���P�[�V�����̃G���g�� �|�C���g���`���܂��B
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

	double price[] = {82.65, 82.45, 0};
	UpdateShortPosition(price);

	TerminateEnv();
	return 0;
}

