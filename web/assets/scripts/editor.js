function setScript(){
	document.forms[0].scriptString.value = document.applets["javascript_editor"].getEditorText();
	return true;
}