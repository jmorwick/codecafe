$( document ).ready(function() {

    var socket = new WebSocket('ws://' + window.location.host + '/jshell');
    var termhome = $('#term-home')
    var terminput = $('#term-input')

    socket.onopen = function() {
    };
    socket.onmessage = function(message) {
        termhome.append(message.data) // print message to terminal
        termhome.scrollTop(termhome[0].scrollHeight - termhome.height()) // scroll to bottom
    };
    terminput.keyup(function(e) {
        if(e.keyCode == 13) {  // return was pressed
            termhome.append(terminput.val()+"\n")  // echo message to terminal
            socket.send(terminput.val())           // send message to server
            termhome.scrollTop(termhome[0].scrollHeight - termhome.height()) // scroll to bottom
            terminput.val('')                      // clear input
        }
    });
})