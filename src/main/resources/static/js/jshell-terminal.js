function getExerciseDiv(innerJQObj) {
    if(innerJQObj.length == 0) {
        // TODO: handle error -- no parent js-exercise div
    }
    if(innerJQObj.attr('class') != 'js-exercise')
        return getExerciseDiv(innerJQObj.parent());
    return innerJQObj;
}

$( document ).ready(function() {

    // connect all history listeners to websockets for their exercises
    $('.js-exercise .js-history').each(function(i) {
        var term = $(this);
        var exerciseId = getExerciseDiv(term).attr('id');
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
    $('.js-exercise .js-sendcode').on('click', function(e) {
        var sendButton = $(this);
        var exerciseId = getExerciseDiv(sendButton).attr('id');
        var codepad = sendButton.parent().find('textarea');
        // TODO: clear messages for this exercise
        $.post('/exercises/'+exerciseId+'/exec', {code: codepad.val()});
        // TODO: report error on failure to send ajax message
        if(codepad.attr('clearonsend') != undefined) codepad.val('');
    });

    // connect all variable listeners to websockets for their exercises
    $('.js-exercise .js-variables').each(function(i) {
        var term = $(this);
        var exerciseId = getExerciseDiv(term).attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/variables');
        socket.onmessage = function(message) {
            term.val(message.data); // print message to terminal
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
        // TODO: sort
        // TODO: make in to a table
    });

    // connect all message listeners to websockets for their exercises
    $('.js-exercise .js-message').each(function(i) {
        var term = $(this);
        var exerciseId = getExerciseDiv(term).attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/errors');
        socket.onmessage = function(message) {
            term.val(message.data); // print error message to terminal
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
        // TODO: sort
        // TODO: make in to a table
    });

    // connect all output listeners to websockets for their exercises
    $('.js-exercise .js-stdout').each(function(i) {
        var term = $(this);
        var exerciseId = getExerciseDiv(term).attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/stdout');
        socket.onmessage = function(message) {
            term.append(message.data); // print message to terminal
            term.scrollTop(term[0].scrollHeight - term.height());  // scroll to bottom
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // connect all output listeners to goals for their exercises
    $('.js-exercise .js-goals').each(function(i) {
        var goalResponses = $(this);
        var exerciseId = getExerciseDiv(goalResponses).attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/goals');
        socket.onmessage = function(message) {
            var pmessage = JSON.parse(message.data);
            var gid = pmessage[0];
            var reason = pmessage[1];
            var progress = pmessage[2];
            $(goalResponses.find('span.progress')[gid]).html((100*progress)+'%');
            $(goalResponses.find('span.reason')[gid]).html(reason);
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors

        // TODO: make in to a table
    });

    // connect all output listeners to goals for their exercises
    $('.js-exercise .js-methods').each(function(i) {
        var glist = $(this);
        var exerciseId = getExerciseDiv(glist).attr('id');
        var socket = new WebSocket('ws://' + window.location.host + '/exercises/'+exerciseId+'/methods');
        socket.onmessage = function(message) {
            JSON.parse(message.data).forEach(function (method) {
                var name = method[0] + ': ' + method[1];
                var option = glist.find('option').filter(function (i,option) { return option.text == name; })[0];
                if(option == null) {
                    glist.append($('<option>', {
                        value: method[2],
                        text: name
                    }));
                } else {
                    option.value = method[2];
                }
            });
        };
        socket.onerror = function(e) { }; // TODO: detect / report connection errors
        socket.onclose = function(e) { }; // TODO: detect / report connection errors
    });
    $('.js-exercise .js-loadmethod').each(function(i) {
        var loadMethod = $(this);
        var glist = getExerciseDiv(loadMethod).find('.js-methods');
        var term = getExerciseDiv(loadMethod).find('.js-codepad textarea');
        loadMethod.on('click', function (ev) {
            term.val(glist.val());
        });
    });

    /*

        $('#stdin').keyup(function(e) {
            if(e.keyCode == 13) {  // return was pressed
                $.post('/exercises/playground/stdin', {data: $('#stdin').val()+"\n"});
                $('#stdin').val('');                     // clear input
            }
        });
        */
});

