#include "stdafx.h"
#include "Mirth Manager.h"

int WINAPI WinMain(HINSTANCE hInstance,HINSTANCE hPrevInstance,LPSTR lpszCmd,int nCmd)
{
	WinExec("java -jar mirth-manager.jar", SW_HIDE);		
	return 0;
}
