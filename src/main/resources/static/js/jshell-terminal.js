$( document ).ready(function() {

    var socket = new WebSocket('ws://' + window.location.host + '/exercises/playground/history');
    var socket2 = new WebSocket('ws://' + window.location.host + '/exercises/playground/variables');
    var socket3 = new WebSocket('ws://' + window.location.host + '/exercises/playground/errors');
    var socket4 = new WebSocket('ws://' + window.location.host + '/exercises/playground/methods');
    var socket5 = new WebSocket('ws://' + window.location.host + '/exercises/playground/stdout');

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
    socket4.onmessage = function(message) {
        JSON.parse(message.data).forEach(function (method) {
            var name = method[0] + ': ' + method[1];
            var option = $('#methods option').filter(function (i,option) { return option.text == name; })[0];
            if(option == null) {
                $('#methods').append($('<option>', {
                    value: method[2],
                    text: name
                }));
            } else {
                option.value = method[2];
            }
        });
    };
    socket5.onmessage = function(message) {
        $('#term-stdout').append(message.data); // print message to terminal
        $('#term-stdout').scrollTop($('#term-stdout')[0].scrollHeight - $('#term-stdout').height()); // scroll to bottom

    };

    $('#sendcode').on('click', function(e) {
            $('#errmsg').val('');
            $.post('/exercises/playground/exec', {code: $('#codepad').val()});
            $('#codepad').val('');                     // clear input
    });

    $('#stdin').keyup(function(e) {
        if(e.keyCode == 13) {  // return was pressed
            $.post('/exercises/playground/stdin', {data: $('#stdin').val()+"\n"});
            $('#stdin').val('');                     // clear input
        }
    });

    $('#loadmethod').on('click', function (e) {
        $('#codepad').val($('#methods option:selected').val());
    });
});

