$( document ).ready(function() {

    // connect all history listeners to websockets for their exercises
    $('.js-exercise .js-history').each(function(i) {
        var term = $(this);
        var exerciseId = term.parent().parent().parent().attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/history');
        socket.onmessage = function(message) {
            term.append(message.data); // print message to terminal
            term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // link every execute button to ajax call for their exercises
    $('.js-sendcode').on('click', function(e) {
        var sendButton = $(this);
        var exerciseId = sendButton.parent().parent().attr('id');
        var codepad = sendButton.parent().find('textarea');
        // TODO: clear messages for this exercise
        $.post('/exercises/'+exerciseId+'/exec', {code: codepad.val()});
        // TODO: report error on failure to send ajax message
        codepad.val('');                     // clear input
    });
/*
    var socket2 = new WebSocket('ws://' + window.location.host + '/exercises/playground/variables');
    var socket3 = new WebSocket('ws://' + window.location.host + '/exercises/playground/errors');
    var socket4 = new WebSocket('ws://' + window.location.host + '/exercises/playground/methods');
    var socket5 = new WebSocket('ws://' + window.location.host + '/exercises/playground/stdout');
    var socket6 = new WebSocket('ws://' + window.location.host + '/exercises/playground/goals');

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
    socket6.onmessage = function(message) {
        var pmessage = JSON.parse(message.data);
        var gid = pmessage[0];
        var reason = pmessage[1];
        var progress = pmessage[2];
        $($('#goals li span.progress')[gid]).html((100*progress)+'%');
        $($('#goals li span.reason')[gid]).html(reason);
    };


    $('#stdin').keyup(function(e) {
        if(e.keyCode == 13) {  // return was pressed
            $.post('/exercises/playground/stdin', {data: $('#stdin').val()+"\n"});
            $('#stdin').val('');                     // clear input
        }
    });
    */
});

