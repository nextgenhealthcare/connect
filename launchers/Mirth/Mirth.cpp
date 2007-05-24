#include "stdafx.h"
#include "Mirth.h"

int WINAPI WinMain(HINSTANCE hInstance,HINSTANCE hPrevInstance,LPSTR lpszCmd,int nCmd)
{
	WinExec("java -jar mirth-launcher.jar launcher.xml", SW_HIDE);
	return 0;
}
