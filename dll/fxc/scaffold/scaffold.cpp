// scaffold.cpp : �R���\�[�� �A�v���P�[�V�����̃G���g�� �|�C���g���`���܂��B
//

#include <stdio.h>
#include "stdafx.h"

extern double __stdcall TrapLots();
extern int __stdcall test();

int _tmain(int argc, _TCHAR* argv[])
{
	double d = TrapLots();
	printf("%f\n", d);

	test();

	return 0;
}

