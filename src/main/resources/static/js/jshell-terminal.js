$( document ).ready(function() {
    var termhome = $('#term-home')
    var t1 = new Terminal()
    t1.setHeight("250px")
    t1.setWidth('650px')
    t1.setBackgroundColor('black')
    t1.blinkingCursor(false)
    termhome.append(t1.html)

    t1.print('Hello, world!')
    t1.input('Whats your name?', function (input) {
        t1.print('Welcome, ' + input)
    })
})