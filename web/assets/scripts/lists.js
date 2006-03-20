// moveVertical() moves options up and down in the second selection list.
// An option must be selected to move it.

function moveVertical(direction, list) {
    var boxLen = list.length;
    var currentItem = list.selectedIndex;
        
    if ((direction == 'up') && (currentItem >= 0)) {
        
        var selText = list.options[currentItem].text;
		var selVal = list.options[currentItem].value;
        var swpText = list.options[currentItem - 1].text;
		var swpVal = list.options[currentItem - 1].value;
            
        list.options[currentItem - 1].value = selVal;
        list.options[currentItem - 1].text = selText;
        list.options[currentItem].value = swpVal;
        list.options[currentItem].text = swpText;
            
        list.selectedIndex = currentItem - 1;
            
     } 
     else if ((direction == 'down') && (currentItem < boxLen - 1) && (currentItem != -1))   {
            
        var selText = list.options[currentItem].text;
		var selVal = list.options[currentItem].value;
        var swpText = list.options[currentItem + 1].text;
		var swpVal = list.options[currentItem + 1].value;
        
        list.options[currentItem + 1].value = selVal;
        list.options[currentItem + 1].text = selText;
        list.options[currentItem].value = swpVal;
        list.options[currentItem].text = swpText;
        
        list.selectedIndex = currentItem + 1;
            
     } 
     else if ((currentItem == -1))
    {           
        alert("You must select an item before you can move it.");
    } 
}

//*****************************************************************************************


// moveHorizontal() moves options from one selection list to another - left or right 
// It finds the selected Options in reverse order, deletes them from the 'from' Select list
// and adds them to the 'to' select list.

function moveHorizontal(theSelFrom, theSelTo) {
    var sfrLength = theSelFrom.length;
    var selectText = new Array();
    var selectVal = new Array();
    var i;
    var count = 0;
    for(i=sfrLength-1; i>=0; i--)
    {
       if(theSelFrom.options[i].selected)
           {
        selectText[count] = theSelFrom.options[i].text;
        selectVal[count] = theSelFrom.options[i].value;
        deleteOption(theSelFrom, i);
        count++;
       }
        
    }
    // Add the selected text/values in reverse order.
    // This will add the Options to the 'to' Select
    // in the same order as they were in the 'from' Select.
    for(i=count-1; i>=0; i--)
    {
            
        addOption(theSelTo, selectText[i], selectVal[i]);
        
    }   

  }


//****************************************************************************

// Helper functions to add and delete options

function deleteOption(theSel, theIndex)
{ 
  var selLength = theSel.length;
  if(selLength>0)
  {
    theSel.options[theIndex] = null;
  }
}

function addOption(theSel, theText, theValue)
{
  var newOpt = new Option(theText, theValue);
  var selLength = theSel.length;
  theSel.options[selLength] = newOpt;
}

//****************************************************************************
function selectAll(theSelect) {
    for (i=0; i < theSelect.length; i++){
        theSelect.options[i].selected = true;
    }    
}



//****************************************************************************


// moveAll() moves all of the contents of one list to the other list

/*function moveAll(theSelFrom, theSelTo)
{
  if(theSelFrom == 'sel1')
  {
    var sfr = document.forms[0].sel1;
    var sto = document.forms[0].sel2;
    var sfrLength = sfr.length;
        var sfrLength = sfr.length;
    var selectText = new Array();
    var selectVal = new Array();
    var i;
    var count = 0;

    for(i=sfrLength-1; i>=0; i--)
    {
        selectText[count] = sfr.options[i].text;
        selectVal[count] = sfr.options[i].value;
        deleteOption(sfr, i);
        count++;
    }

    for(i=count-1; i>=0; i--)
    {
            addOption(sto, selectText[i], selectVal[i]);
    }   
   }

  else
  {
    var sfr = document.forms[0].sel2;
    var sto = document.forms[0].sel1;
    var sfrLength = sfr.length;
    var selectText = new Array();
    var selectVal = new Array();
    var i;
    var count = 0;
    
    // Getting and deleting options from 'from' list
    for(i=sfrLength-1; i>=0; i--)
    {     
        selectText[count] = sfr.options[i].text;
        selectVal[count] = sfr.options[i].value;
        deleteOption(sfr, i);
        count++;
            
    }

    for(i=count-1; i>=0; i--)
    {
            addOption(sto, selectText[i], selectVal[i]);
    }   

  }
}*/

