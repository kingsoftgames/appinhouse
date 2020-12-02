function error_msg(content,delay){
      $('#alert_msg').empty()
      $('#alert_msg').append('<div class="alert alert-danger alert-dismissable">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>'+
                        '<strong>'+content+'</strong>'+
                      '</div>');
      setTimeout("remove_msg()",delay)
    }
    function succ_msg(content,delay){
      $('#alert_msg').empty()
      $('#alert_msg').append('<div class="alert alert-success alert-dismissable">'+
                        '<button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>'+
                        '<strong>'+content+'</strong>'+
                      '</div>');
      setTimeout("remove_msg()",delay)
  
    
    }
    function remove_msg(){
      $('#alert_msg').empty()
    }