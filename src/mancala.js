// sets up the HTML elements that display the mancala board
function initMancala() {
    makeMancala();

    // initialize global variables to keep track of game state
    board = ""; // board representation
    turn = 1;      // the current player
    p1 = 0;
    p2 = 0;
    move = -1;
    state = "";
}

// makes the HTML elements to hold the board
function makeMancala() {
    var display = $("#display");
    var p1D = $("")
    var m = $("#mancala0");
    for (var i = 10; i > 5; i--) {
        display.append(m.clone());
    }
    display.append("<br>");
    for (var i = 0; i < 6; i++) {
        display.append(m.clone());
    }

    $(".mancala").click(selectMancala);
}

function selectMancala() {
    if ($(this).attr('data-selected') == 'false') {
        $(".mancala").each(function () {
            $(this).attr('data-selected', 'false');
        });
        $(this).attr('data-selected', 'true');
    }
    else {
        $(this).attr('data-selected', 'false');
    }
}

function isMove(elt) {
    if ($(elt).attr('data-selected') == 'true') {
        return true;
    }
    else {
        return false;
    }
}

function encodeState() {
    var mArray = new Array(12);
    $(".mancala").each(function (i) {
        if (i < 6)
            mArray[11 - i] = $(this).text();
        else
            mArray[i - 6] = $(this).text();
        if (isMove(this))
            move = i;
    });
    var mancala = "";
    mArray.forEach(function (val) {
        mancala += val + ",";
    });
    state = mancala + ";" + p1 + ";" + p2 + ";" + (turn == 1 ? true : false) + ";" + move;
    console.log(state);
    return state;
}

function moveStones() {
    state = encodeState();
    $.getJSON("http://localhost:9090/move.html?" + state, doMove);
}

function hint() {
    state = encodeState();
    $.getJSON("http://localhost:9090/hint.html?" + state, showHint);
}

function showHint(result) {
    console.log(result);
    if (result.state == state) {
        if ("message" in result) {
            $("#message").text(result.message);
        }
        else {
            $(".mancala").each(function (i) {
                if (i == result.hint)
                    $(this).attr('data-selected', 'true');
                else
                    $(this).attr('data-selected', 'false');
            });
        }
    }
}

function doMove(result) {
    console.log(result);
    if (result.state == state) {
        if ("message" in result) {
            $("#message").text(result.message);
        }
        else {
            $("#message").text("");
            state = result.mancala + ";" + result.p1 + ";" + result.p2 + ";" + result.turn;
            turn = result.turn;
            //$("#player").text(turn);
            if (result.over == "true") {
                $("#player").text(result.winner + " wins!");
            }
            else {
                $("#player").text(turn + "'s turn");
            }
            var board = result.mancala.split(',');
            $(".mancala").each(function (i) {
                $(this).attr('data-selected', 'false');
                if (i < 6)
                    $(this).text(board[11 - i]);
                else
                    $(this).text(board[i - 6]);
            });
            p1 = result.p1;
            p2 = result.p2;
            $('#p1').text(result.p1);
            $('#p2').text(result.p2);
        }
    }
}