 function getTimeOffset(){
    var d = new Date()
    var offset=d.getTimezoneOffset()
    return -1*offset
 }