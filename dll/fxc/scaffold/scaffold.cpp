// scaffold.cpp : �R���\�[�� �A�v���P�[�V�����̃G���g�� �|�C���g���`���܂��B
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

