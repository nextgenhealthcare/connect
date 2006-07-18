/***********************************************************/
/* Modifies any element with class deleteButton to show an alert when the user
* clicks on the button/link to confirm this deletion operation. If user accepts, it returns true, otherwise
* it returns false
 */
function addConfirmDelete() {
 if (!document.getElementsByTagName) return;
 var deleteButtons = document.getElementsBySelector(".deleteButton");
 
 for (var i=0; i<deleteButtons.length; i++) {
   var deleteButton = deleteButtons[i];

   deleteButton.onclick = function(){
        return confirm('Are you sure you want to delete this item?');
        };
     }
 }
 
 function addCantDelete() {
  if (!document.getElementsByTagName) return;
  var noDeleteButtons = document.getElementsBySelector(".noDeleteButton");
 
  for (var i=0; i<noDeleteButtons.length; i++) {
    var noDeleteButton = noDeleteButtons[i];

    noDeleteButton.onclick = function(){
        return alert('This item is in use by one or more channels, you must first delete the channels that are using it.');
        };
     }
 }
 /***********************************************************/
/* Modifies inputs with class deleteButton to change the hidden input called op to "delete" 
so that when the user clicks a deleteButton, the form is submited and its operation is delete instead of the 
regular submit (edit) operation
This function must be called *after* adding confirmDelete() to the onclick handler of the buttons
 */
function addDeleteButtonsAction() {
    if (!document.getElementsByTagName) return;
    var deleteButtons = document.getElementsBySelector(".buttonNav input.deleteButton");

    for (var i=0; i < deleteButtons.length; i++) {
        var deleteButton = deleteButtons[i];        
        var onclickFunction = deleteButton.onclick;
        
        deleteButton.onclick = function(){

        if (onclickFunction()) {
                var parent = this.form;
                
                if (parent != null){//we found the parent form item
                    //find the op element
                    var op = parent.op;
                     if (op != null) {
                        op.value = "delete";
                    }
                    parent.submit();
                }
            }           
        };
     }
}
 
 /***********************************************************/
/* Modifies selects with class "showOptions" so that their onchange event
* triggers a show/hide of more options for this selection.
* On every onchange event, it goes through every option hiding the elements with
* class "optionvalue_options" and then shows the one matching the currently
* selected value
 */
function hideShowSetUp() {
    if (!document.getElementsByTagName) return;
    var selects = document.getElementsBySelector("select.showOptions");
    
    for (var i=0; i < selects.length; i++) {
      var thisSelect = selects[i];

      thisSelect.onchange = function(){
          //hide all options in select
          var allChildren = this.childNodes;
          for (var j = 0; j < allChildren.length; j++){
              var optionsFieldset = document.getElementById(allChildren[j].value + "_options");
              if (optionsFieldset != null){
                  optionsFieldset.style.display = "none";
              }
          }
          
          //show selected options
          var optionsFieldset = document.getElementById(this.value + "_options");
          if (optionsFieldset != null){
              optionsFieldset.style.display = "block";
          }
      };
    }
}

 /***********************************************************/
/* Shows/hides help box next to form inputs.
When a field with class "hasHelp" is focused, it hides all elements with class "help" 
and then it looks for the element with class "help" and id matching the field name + "_help"
(fieldName_help) and shows it.
*/
function helpSetUp() {
    if (!document.getElementsByTagName) return;
    var elements = document.getElementsBySelector(".hasHelp");
    
    for (var i=0; i < elements.length; i++) {
      var thisElement = elements[i];

      thisElement.onfocus = function(){
          //hide current help
          var allChildren = document.getElementsBySelector(".help");
          if (allChildren != null){
            for (var j = 0; j < allChildren.length; j++){
                allChildren[j].style.display = "none";
            }
          }
          
          //show current help         
            var visibleElement = document.getElementById(this.name + "_help");
              if (visibleElement != null){
                  visibleElement.style.display = "block";
              }
      };
      
      thisElement.onblur = function(){
          //hide current help
          var allChildren = document.getElementsBySelector(".help");
          if (allChildren != null){
            for (var j = 0; j < allChildren.length; j++){
                allChildren[j].style.display = "none";
            }
          }          
        
      };
      
    }
}

 /***********************************************************/
/* Gets all the select elements with class "dualSelect" and adds another
select to move  items from one to the other
*/
function addDualSelects(){
    if (!document.getElementsByTagName) return;
    var elements = document.getElementsBySelector(".dualSelect");
    var previousForm = null;
//@TODO: check that parent form is not null
    for (var i=0; i < elements.length; i++) {
        var name = elements[i].name;
        var parentForm = elements[i].form;
        
        //look for right/left buttons
        var right = parentForm[name + "_rightbtn"];
        var left = parentForm[name + "_leftbtn"];
        
        //look for up/down buttons
        var up = parentForm[name + "_upbtn"];
        var down = parentForm[name + "_downbtn"];
        var dual = parentForm[name + "_dual"];
        
         if (dual != null){
            //add a double-click event to the select
            dual.ondblclick = function() {
                var name = this.name.substring(0,this.name.indexOf("_dual"));
                var parentForm = this.form;
                //look for select
                var originalSelect = parentForm[name];
                moveHorizontal(this,originalSelect);
            }
            //add a double-click event to the select
            elements[i].ondblclick = function() {
                var parentForm = this.form;
                //look for dual select
                var originalSelect = parentForm[this.name + "_dual"];
                moveHorizontal(this,originalSelect);
            }
          }
        
        if (right != null) {
            right.onclick = function() {
                var name = this.name.substring(0,this.name.indexOf("_rightbtn"));
                var parentForm = this.form;
                //look for dual
                var dual = parentForm[name + "_dual"];
                var thisSelect = parentForm[name];
                
                moveHorizontal(dual,thisSelect);
            }
        }
        
        if (left != null) {
            left.onclick = function() {
                var name = this.name.substring(0,this.name.indexOf("_leftbtn"));
                var parentForm = this.form;
                //look for dual
                var dual = parentForm[name + "_dual"];
                var thisSelect = parentForm[name];
                
                moveHorizontal(thisSelect,dual);
            }
        }
        
        if (up != null) {
            up.onclick = function() {
                var name = this.name.substring(0,this.name.indexOf("_upbtn"));
                var parentForm = this.form;
                var thisSelect = parentForm[name];
                
                moveVertical('up',thisSelect);
            }
        }
        
        if (down != null) {
            down.onclick = function() {
                var name = this.name.substring(0,this.name.indexOf("_downbtn"));
                var parentForm = this.form;
                var thisSelect = parentForm[name];
                
                moveVertical('down',thisSelect);
            }
        }
        
        //add a form submit event
        if (previousForm != parentForm){
            addSubmitEvent(parentForm, function() {
                var selects = parentForm.getElementsByTagName("select");
                 for (var j=0; j < selects.length; j++) {
                    if (selects[j].className.indexOf("dualSelect")  != -1){
                        selectAll(selects[j]);
                    }
                 }
                return true;
            });
            previousForm = parentForm;
        }
        
    }    
}

/***********************************************************/
/* Modifies selects with class "autoSubmit" so that their onchange event
* triggers a form submit
*/
function selectAutoSubmitSetUp() {
    if (!document.getElementsByTagName) return;
    var selects = document.getElementsBySelector("select.autoSubmit");
    
    for (var i=0; i < selects.length; i++) {
      var thisSelect = selects[i];

      thisSelect.onchange = function(){
          this.form.submit();
      };
    }
}




/************************************************************/
/*Onload event */
function addLoadEvent(func) {
    var oldonload = window.onload;
    if (typeof window.onload != 'function') {
      window.onload = func;
    } else {
     window.onload = function() {
       oldonload();
       func();
     }
   }
 }
 
/************************************************************/
/* Onsubmit event */
function addSubmitEvent(form, func) {
    var oldonsubmit = form.onsubmit;
    if (typeof form.onsubmit != 'function') {
       form.onsubmit = func;
    }
    else {
        form.onsubmit = function() {
            if (oldonsubmit());
                return func();
        }
    }
 }

/***********************************************************/
//addConfirmDelete must be called before calling addDeleteButtonsActions()
addLoadEvent(addConfirmDelete);
addLoadEvent(addCantDelete);
addLoadEvent(addDeleteButtonsAction);
addLoadEvent(hideShowSetUp);
addLoadEvent(helpSetUp);
addLoadEvent(addDualSelects);
addLoadEvent(selectAutoSubmitSetUp);
