$( document ).ready(function() {

    var socket = new WebSocket('ws://' + window.location.host + '/jshell/ex1');
    var termhome = $('#term-home')
    var terminput = $('#term-input')
    var varshome = $('#term-vars')

    var socket2;

    socket.onmessage = function(message) {
        if(socket2 == null) {
            socket2 = new WebSocket('ws://' + window.location.host + '/vars/ex1');
            socket2.onmessage = function(message) {
                varshome.val(message.data)
            }
        }
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

