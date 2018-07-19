$( document ).ready(function() {

    var socket = new WebSocket('ws://' + window.location.host + '/lessons/ex1/history');
    var socket2 = new WebSocket('ws://' + window.location.host + '/lessons/ex1/variables');
    var socket3 = new WebSocket('ws://' + window.location.host + '/lessons/ex1/errors');

    socket.onmessage = function(message) {
        $('#term-home').append(message.data); // print message to terminal
        $('#term-home').scrollTop($('#term-home')[0].scrollHeight - $('#term-home').height()); // scroll to bottom

    };
    socket2.onmessage = function(message) {
        $('#term-vars').val(message.data);
    };
    socket3.onmessage = function(message) {
        $('#errmsg').val(message.data);
    };

    $('#code1').keyup(function(e) {
        if(e.keyCode == 13) {  // return was pressed
            $('#errmsg').val('');
            $.post('/lessons/ex1/exec', {code: $('#code1').val()});
            $('#code1').val('');                     // clear input
        }
    });
});

