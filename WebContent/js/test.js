//window.onload = function() {
  alert('Onload');
//}
//window.addEventListener('load', addTags, false); 
//window.addEventListener('load', addTags, false); 
if (document.addEventListener) {
    document.addEventListener("DOMContentLoaded", addTags, false);
}
//window.onload = addTags;

function addTags(){
    // quit if this function has already been called
    if (arguments.callee.done) return;

    // flag this function so we don't do the same thing twice
    arguments.callee.done = true;

    // do stuff
    alert(document.title);
};
