import T::std::Vector
import T::std::HashMap
import T::std::HashSet
import T::std::Comparator
import java::lang::Math
import java::util::Arrays
import java::lang::StringBuilder
import java::util::regex::Matcher
import java::util::regex::Pattern

val input = `              /----------------------\                                                                              /--------------------------\
              |                     /+------------------------------------------------------------------------------+-------------\            |
              |                  /--++--------------------------------------------------------\                     |             |            |
              |                  | /++--------------------------------------------------------+---------------------+-------\     |            |
              | /----------------+-+++--------\ /---------------------------------------------+---------------------+\      |     |            |
              | |                | |||        | |                                            /+---------------------++------+-----+-----------\|
         /----+-+-\              | |||        | |               /----------------------------++---------------------++---\  |     |           ||
         |    | | |/-------------+-+++--------+-+---------------+--->------------------------++--\     /------------++---+--+-----+---------\ ||
         |    | | ||     /-------+-+++--------+-+---------------+----------------------------++--+-----+-----\      ||   |  |     |         | ||
        /+----+-+-++-----+-------+-+++--------+-+---------------+----------------------------++--+--\  |     |      ||   |  |     |         | ||
        ||    |/+-++-----+-------+-+++--------+-+---------------+----------------------------++--+--+--+-----+-\    ||   |  |     |         | ||
  /-----++----+++\||     |       | |||        | |               |                            ||  |  |  |     | |    ||   |  |  /--+---------+-++----\
  |     ||    ||||||     |       | |||        | |            /--+----------------------------++--+--+--+-----+-+----++---+--+--+--+------\  | ||    |
  |     ||    ||||||     |       | |||        | |   /--------+\ |      /---------------------++--+--+--+-----+-+----++---+--+--+--+------+--+-++---\|
  |     ||    ||||||/----+-------+-+++--------+-+---+--------++-+------+---------------------++--+--+--+-----+-+----++---+\ |  |  |      |  | ||   ||
  |     ||    |||||||/---+-------+-+++--------+-+---+--------++-+------+----------------\    ||  |  |  |     | |    ||   || |  |  |      |  | ||   ||
  |     ||    ||||||||   |       | |||        | |   |        || |      |                |    ||  |  |  |     | |  /-++---++-+--+--+------+--+-++-\ ||
  |  /--++----++++++++---+-------+-+++--\   /-+-+---+--------++-+------+----------------+----++--+--+\ |     | |  | ||   || |  |  |      |  | || | ||
 /+--+--++----++++++++---+-------+-+++--+---+-+-+---+--------++-+------+----------------+-\  ||  |  || |     | |  | ||   || |  |  |      |  | || | ||
 ||  |  ||    ||||||||   |       | |||  |   | | |   |        || |      |                | |  ||/-+--++-+-----+-+--+-++\  || |  |  |      |  | || | ||
 ||  |  ||    ||||||||   |       | |||  |   | | |   |        || |      |       /--------+-+--+++-+--++\|     | |  | |||  || |  |  |      |  | || | ||
 ||  |  ||    ||||||||   |       | |||  |   | | |   |        || |      |       |        | |  ||| |  ||||     | |  | |||  || |  |  |      |  | || | ||
 ||  |  ||    |||||^||   \-------+-+++--+---+-+-+---+--------++-+------+-------+--------+-+--+++-+--++++-----/ |  | |||  || |  |  |      |  | || | ||
 ||  |  \+-<--++++++++-----------+-+++--+---+-+-+---+--------++-+------+-------+--------+-+--+++-+--/|||       |  | |||  || |  |  |      |  | || | ||
 ||  |   |    ||||||||      /----+-+++--+---+-+-+---+--------++-+----\ |       |        | |  ||| |   |||/------+--+-+++--++-+--+--+------+--+\|| | ||
 ||  |   |    ||||||||      |    | |||/-+---+-+-+---+--------++-+----+-+-------+--------+-+--+++-+---++++------+--+-+++--++-+--+--+------+-\|||| | ||
 ||  |   |    ||||||||      |    | |||| |  /+-+-+---+--------++-+----+-+-------+--------+-+--+++-+---++++------+--+-+++--++-+--+--+--\   | ||||| | ||
 ||  |   |    ||||||||      |    | \+++-+--++-+-+---+--------++-+----+-+-------+--------+-+--+++-+---++++------+--+-+++--++-/  |  |  |   | ||||| | ||
 ||  |   |    ||||||||      |    |  ||| |/-++-+-+---+--------++-+----+\|       |        | |  ||| |   ||||      |  | |||  ||    |  |  |   | ||||| | ||
 ||  |/--+----++++++++---->-+----+--+++-++-++-+-+---+--------++-+---\|||       |        | |  ||| |   ||||      |/-+-+++--++----+--+\ |   | ||||| | ||
 ||  ||  |    ||||||||     /+----+--+++-++-++-+-+---+--------++-+---++++-------+--------+-+--+++-+---++++------++-+-+++--++---\|  || |   | ||||| | ||
 ||  ||  |    ||||||||     ||    | /+++-++-++\| |   |        || ^   ||||       |        | |  ||| |   ||||/-----++-+-+++--++---++--++-+---+\||||| | ||
 ||  ||  |/---++++++++-----++----+-++++-++-++++-+---+--------++-+---++++-------+--------+-+--+++-+\/-+++++-----++-+-+++--++---++\ || |   ||||||| | ||
 ||  ||  ||   ||||||||     ||    | |||| || |||| |   |        || |   ||||       |        | |  ||| ||| |||||     || | |||  ||   ||| || |   ||||||| | ||
 ||  ||  ||   ||||||||     ||    | |||| || ||||/+---+--------++-+---++++-------+--------+-+\ ||| ||| |||||     || | |||  ||   ||| || |   ||||||| | ||
 ||  || /++---++++++++-----++----+-++++-++-++++++---+--------++-+---++++-------+--------+-++-+++-+++-+++++\    || | |||  ||   ||| || |   ||||||| | ||
 ||  || |||   ||||||||     ||    |/++++-++-++++++---+--------++\|   ||||       |        |/++-+++-+++-++++++--\ || | |||  ||   ||| || |   ||||||| | ||
 ||  || |||   ||||||||     ||    |||||| || ||||||   |        ||||   ||||       \--------++++-+++-+++-+/||||  | || | |||  ||   ||| || |   ||||||| | ||
 |\--++-+++---+++/||||     ||    |||||| || ||||||   |        ||||   |||| /--------------++++-+++-+++-+-++++--+-++-+-+++--++---+++-++-+\  ||||||| | ||
 |   \+-+++---+++-++++-----++----++++++-/| ||||||   |        ||||   |||| |              |||| ||| ||| | ||||  | || | |||  ||   ||| || ||  ||||||| | ||
 |    | |||   ||| ||||     ||    |||||| /+-++++++---+--------++++---++++-+--------------++++-+++-+++-+-++++--+\|| | |||  ||   ||| || ||  ||||||| | ||
 |    | |||   |\+-++++-----++----++++++-++-++++++---+--------++++---++++-+--------------++++-+++-+++-+-++++--++/| | |||  ||   ||| || ||  ||||||| | ||
 |    | |||   | | ||||     ||/---++++++-++-++++++---+--------++++---++++-+------------\ |||| ||| ||| | ||\+--++-+-+-+++--++---+++-++-++--+/||||| | ||
 |    | |||   | | ||||     |||   |||||| || ||||||   | /------++++---++++-+------------+-++++-+++-+++-+-++-+--++-+-+-+++--++--\||| || ||  | ||||| | ||
/+----+-+++---+\| ||||     |||   |||||| || ||||||   | |      ||||   |||| |            | |||| ||| ||| | |\-+--++-+-+-+++--++--++++-++-++--+-++/|| | ||
||    | |||   ||| ||||     |||   |||||| || ||||||   \-+------+/|\---++++-+------------+-++++-+++-+++-+-+--+--++-+-+-+++--/|  |||| || ||  | || || | ||
||  /-+-+++---+++-++++-\   |||   |||||| |\-++++++-----+------+-+----++/| |       /----+-++++-+++-+++-+-+--+--++-+-+\|||   |  |||| || ||  | || || | ||
||  | | |||   ||| |||| |   |||   |||||| |  ||||\+-----+------+-+----++-+-+-------+----+-+++/ ||| ||| | |/-+--++-+-+++++--\|  |||| || ||  | || || | ||
||  | | |||   ||| |||| |   |||   |||||| |  |||| |     |      | |    || | |       |    | |||  ||| ||| | || |  || | |||||  ||  |||| || ||  | || || | ||
||  |/+-+++---+++-++++-+---+++---++++++-+--++++-+-----+------+-+----++-+-+-------+---\| |||  ||\-+++-+-++-+--++-+-++++/  ||  |||| || ||  | || || | ||
||  ||| ||\---+++-++++-+---+++---++++++-+--++++-+-----+------+-+----++-+-+-------+---++-+++--++--+/| | \+-+--++-+-++++---++--++++-++-++--+-+/ || | ||
||  ||| ||/---+++-++++-+---+++---++++++-+--++++-+-----+------+-+----++-+-+-----<-+---++-+++--++-\| | |  | |  || | ||\+---++--++++-++-++--+-+--+/ | ||
||  ||| |||   ||| |||| |   |||   |||||| |  |||| |     |      | |    || | |       |   || |||  ||/++-+-+--+-+--++-+-++-+---++\ |||| || ||  | |  |  | ||
||  ||| |||   \++-++++-+---+++---++++/| |  ||||/+-<---+------+-+----++-+-+-------+---++-+++--+++++-+\|  | |  || | || |   ||| |||| || ||  | |  |  | ||
||  ||| |||    || |||| |   |||   |||| | |  ||||||     |      | |  /-++-+-+-------+---++-+++--+++++-+++--+-+-\|| | || |   ||| |||| || ||  | |  |  | ||
||  ||| |||    || |||| |   |||   |||| | |  ||||||     |      | |  | || | |       |   || |\+--+++++-+++--+-+-+/|/+-++-+---+++-++++-++-++--+-+\ |  | ||
||  ||| |||    || |||| |   |||   |||| | |  ||||||     |      | |  | || | |       ^   || | |  ||||| |||  | | | ||| || |   ||| |||| || ||  | || |  | ||
||  ||| |||    || |||| |   |||   |||| | |  ||||||  /--+------+-+--+-++-+-+-------+---++-+-+--+++++-+++--+-+-+-+++-++-+---+++-++++-++-++\ | || |  | ||
||  ||| |||    || |||| |   |||   |||| | |  ||||||  |  |      | |  | || | |       \---++-+-+--+++++-+++--+-+-+-+++-+/ |   ||| |||| || ||| | || |  | ||
|\--+++-+++----++-++++-+---+++---++++-+-+--++++++--+--+------+-+--+-++-+-+-----------++-+-/  ||||| |||  | | | ||| |  |   ||| |||| || ||| | || |  | ||
|   ||| |||    || |||| |   |||   |||| | |  ||||||  | /+------+\|  | || | |           || |    ||||| |||  | | | ||| |  |   ||| |||| || ||| | || |  | ||
|   ||| |||    || |||| |   |||   |||| | |  ||||||  | ||      |||  | || | |/----------++-+----+++++-+++--+-+-+-+++-+--+---+++-++++-++\||| | || |  | ||
|   ||\-+++----++-++++-+---+++---++++-+-+--++++++--+-++------+++--+-/| | ||          || |    ||||| |||  | | | |||/+--+---+++-++++-++++++-+-++-+--+\||
|/--++--+++----++-++++-+\  |||   |||| | |/-++++++--+-++------+++--+--+-+-++----------++-+----+++++-+++--+-+-+-+++++--+---+++-++++<++++++-+-++-+\ ||||
||  ||  |||  /-++-++++-++--+++---++++-+-++-++++++--+-++------+++--+--+-+-++----------++-+----+++++-+++--+-+-+-+++++--+\  ||| |||| |||||| | || || ||||
||  ||  |||  | || ||||/++--+++---++++-+-++-++++++--+-++------+++--+--+-+-++----------++-+----+++++-+++--+-+-+\|||||  ||  ||| |||| |||||| | || || ||||
||/-++--+++--+-++-+++++++--+++---++++-+\|| |||||\--+-++------+++--+--+-+-++----------++-+----+++++-+++--+-+-+++++++--/|  ||| |||| |||||| | || || ||||
||| ||  |||  | || |||||||  |||   |||| |||| ||||| /-+-++------+++--+--+-+-++--------\ || |    ||||| |||  | | |||||||  /+--+++-++++-++++++-+-++-++\||||
||| ||  |||  | || |||||||  |||   |||| |||| ||||| | | ||      |||  |  | | ||        | || |    ||||| |||  | | |||||||  ||  ||| |||| |||||| | || |||||||
||| ||  |||  | || |||||||  |\+---++++-++++-+++++-+-+-++------+++--+--/ | ||    /---+-++-+----+++++-+++--+-+-+++++++--++--+++-++++\|||||| | || |||||||
||| ||  |||  | || ||\++++--+-+---++++-++++-+++++-+-+-++------+++--+----+-++----+---+-++-+----+++++-+++--+-+-+++++++--++--+/| ||||||||||| | || |||||||
||| ||  |||  | || || ||||  | |   |||| |||| \++++-+-+-++------+++--+----+-++----+---+-++-+----+++++-+++--+-+-+++++++--++--+-+-++++++++/|| | || |||||||
||| ||  |||  | || || |v||  | |  /++++-++++--++++-+-+-++------+++--+----+-++-\  |   | || |    ||||| |||  | | |||||||  ||  | | |||||||| || | || |||||||
||v || /+++--+-++-++-++++--+-+--+++++-++++--++++-+-+-++------+++--+----+-++-+--+---+-++-+----+++++-+++--+-+\|||||||  ||  | | |||||||| || | || |||||||
||| || ||||  | || || ||||  | |  ||||| ||||  |||| | | ||      |||  |/---+-++-+-\|   | || |    ||||| |||  | |||||||||  ||  | | |||||||| || | || |||||||
||| || ||||  | || || ||||  | |  ||||| ||||  |||| | | ||      |||  ||   | || | ||/--+-++-+----+++++-+++--+-+++++++++\ ||  | | |||||||| || | || |||||||
||| || ||||  | || || ||||  | |  |||\+-++++--+/|| | | ||      |||  ||   \-++-+-+++--+-++-+----+++++-+++--+-++++++++++-++--+-+-++++++++-++-+-++-+++++/|
||| || ||||  | || || ||||  | |  ||| | ||||  | || | | ||      |||  ||     || | |||  | || |    ||||| |||  | |||||||||| \+--+-+-++++++++-++-+-++-++/|| |
||| || ||||  \-++-++-++++--+-+--+++-+-++++--+-++-+-+-++------+++--++-----++-+-+++--+-++-+----+++++-+++--+-++++++++++--/  | | ||\+++++-++-+-++-++-++-/
||| || ||||    || || ||||  | |  ||| | \+++--+-++-+-+-++------+++--++-----++-+-+++--+-++-+----+++++-+++--+-++++++++++-----+-+-++-+++++-++-+-/| || ||
||| || ||||    ||/++-++++--+-+--+++-+--+++--+-++-+-+-++------+++--++-----++-+-+++--+-++-+----+++++-+++--+-++++++++++-----+-+\|| ||||| || |  | || ||
||| || |||\----+++++-++++--+-+--+++-+--+++--+-++-+-+-++------+++--++-----++-+-+++--+-++-+----+++/| |||  | ||||||||||     | |||| ||||| || |  | || ||
||| || |||     ||||| ||||  | |  ||| |  |||  | || | | ||      |||  ||     || | |||  | || |    ||| | |||  \-++++++++++-----/ |||| ||||| || |  | || ||
|\+-++-+++-----+++++-+++/  | |  ||| |  ||\--+-++-+-+-++------+++--++-----++-+-+++--+-++-+----+++-+-+++----++++++++++-------++++-+++++-++-+--+-+/ ||
| | || |||     ||||| |||   | |  ||| |  ||   | || | | ||      |||  ||     || | |||/-+-++-+----+++-+-+++----++++++++++-------++++-+++++-++-+\ | |  ||
| | || |||     ||||| ||| /-+-+--+++-+--++\  | || | | || /----+++--++-----++-+-++++-+-++-+----+++-+-+++----++++++++++-------++++-+++++-++-++-+-+--++-\
| | || |||     ||||| ||| | | |  ||| |  |||  | || | | || |    |||  ||     || | |||| | || |    \++-+-+++----++++++++++-------++++-+++++-++-++-+-/  || |
| | || ||\-----+++/| ||| | | |  ||| |  |||  | || | | || |    |||  ||     || | |||| | || |     |\-+-+++----++++++++++-------/||| ||||| || || |    || |
| | || ||  /---+++-+-+++-+-+-+--+++-+--+++--+-++-+-+-++-+----+++--++-----++-+-++++-+-++-+-----+--+-+++----++++++++++--\     ||| ||||| || || |    || |
\-+-++-++--+---/|| | ||| | | |  ||| |  |||  | || | | || |    |||  ||     || | |||| | || |     |  | \++----++++++++++--+-----+++-/|||| || || |    || |
  | || ||  |    || | ||| | | |  ||| |  |||  | || | | || |    |||  ||     || | |||| | || |     |  |  ||    ||||||||||  |     |||  |||| || || |    || |
  | || ||  |    \+-+-+++-+-+-+--+++-+--+++--+-/| | | || |    |||  ||     || | |||| | || |     |  |  ||/---++++++++++--+-----+++--++++-++-++-+\   || |
  | || || /+-----+-+-+++-+-+-+--+++-+--+++--+--+-+-+-++-+----+++-\||     || | |||| | || |     |  |  |||   ||||||||||  |     |||  ||^| || || ||   || |
  | || || ||     | | ||| | | |  ||| |  |||  |  | | | || |    ||| |||     || | |||| | || |     |  |  |||   ||||||||||  |     |||  |||| || || ||   || |
  | || || ||     | | ||| | | |  ||| |  |||  |  | | | || |    ||| |||     || | ||||/+-++-+-----+--+-\|||   ||||||||||  |     |||  |||| || || ||   || |
/-+-++-++-++-----+-+-+++-+-+-+--+++-+--+++--+--+-+-+-++\|    ||| |||     || | |||\++-++-+-----+--+-++++---++++++++++--+-----+++--++++-++-+/ ||   || |
| | ||/++-++-----+-+-+++-+-+-+--+++-+--+++--+--+-+-+-++++----+++-+++\    || | ||| || || |     |  | ||||   ||||||||||  |     |||  |||| || |  ||   || |
| | ||||| ||     | | ||| | |/+--+++-+--+++--+--+-+-+-++++----+++-++++----++-+-+++-++-++-+-----+--+-++++---++++++++++--+-----+++-\|||| || |  ||   || |
| | |||||/++-----+-+-+++\| |||  ||| \--+++--+--+-+-+-++++----+++-++++----++-+-+++-++-++-+-----+--+-++++---++++++++++--+-----+++-++/|| || |  ||   || |
| | ||||||||     | | ||||| |||  |\+----+++--+--+-+-+-++++----+++-++++----++-+-+++-++-++-+-----/  | ||||   ||||||||\+--+-----+++-++-++-++-+--++---/| |
| | ||||||||     | | ||||| |||  | |    |||  |  | | | ||||    ||| ||||    || | ||| || || |        | ||||   |||||||| |  |     ||| || || || |  ||    | |
| | ||||||||     | | ||||| |||  | |    |||  |  | | | ||||    \++-++++--->++-+-+++-++-++-+--------+-++++---++++++++-+--+-----+++-++-++-++-/  ||    | |
| | ||||||||     | | ||||| |||  | |  /-+++--+--+-+-+-++++-----++-++++\   || | ||| || || |        | ||||   |||||||| |  |     ||| || || ||    ||    | |
| | ||||||||     | | ||||| |||  | |  | |||  |  | | | ||||     || |||||   || | ||| || || |        | ||||   |||||||| |  |     ||| || || ||    ||    | |
| | ||||||||     | | ||||| |||  \-+--+-+++--+--+-+-+-++++-----++-+++++>--++-/ ||| || || |        | ||||   |||||||| |  |     ||| || || ||    ||    | |
| | ||||||||     | | ||||| |||   /+--+-+++--+--+\| | ||||     || |||||   ||   ||| || || |        | |||\---++++++++-+--+-----+++-++-++-++----+/    | |
| \-++++++++-----+-+-+++++-+++---++--+-/||  |  ||| | ||||     || |||||  /++---+++-++-++-+--------+\|||    |||||||| |  |     ||| || || ||    |     | |
|   ||||||||     | | ||||| |||   ||  |  ||  |  \++-+-++++-----++-+++++--+++---+++-++-++-+--------+++/|    |||||||| |  |     ||| || || ||    |     | |
|   ||||||\+-----+-+-+++++-+++---++--+--++--+---++-+-++++-----++-/||||  |||   ||| || ||/+--------+++-+----++++++++-+--+-----+++-++-++-++----+\    | |
|   |||||| |     | \-+++++-+++---++--+--++--+---++-+-++++-----++--++++--+++---+++-++-++++--------/|| |    ||||||\+-+--+-----+++-++-/|/++----++\   | |
|   |||||| |     |   ||||| |||   ||  |  \+--+---++-+-++++-----++--++++--+++---+++-++-++++---------++-+----++++/| | |  |     ||| ||  ||||    |||   | |
|   |||||| |     |   ||||| |||   ||  |   |  |   || | ||||     ||  ||||  |\+---+++-++-++++---------++-+----++++-+-+-+--+-----+++-++--++/|    |||   | |
|   |||||| |    /+---+++++-+++---++--+---+--+---++-+-++++-----++--++++--+-+---+++-++-++++---------++-+----++++-+-+-+--+\    ||| ||  || |    |||   | |
|   |||||\-+----++---+++/| |||   ||  |   |  |   || | ||||     ||  ||||  | |   ||| || ||||         || |    |||| | | |  ||    ||| ||  || |    |||   | |
|   |||||  |  /-++---+++-+-+++---++--+---+--+---++-+-++++-----++--++++-\| \---+++-++-++++---------++-+----++++-+-+-+--++----+++-++--/| |    |||   | |
|   |||||  |  | ||   ||| | ||\---++--+---+--+---++-+-++++-----++--++++-++-----+++-++-+/||         || |  /-++++-+-+-+--++----+++-++---+-+----+++-\ | |
|   |||||  |  | ||   ||| | ||    \+--+---+--+---/| | ||||     ||  |||| ||     ||| || | ||         || |  | |||| | | |  ||   /+++-++---+-+----+++-+-+\|
|   |||||  |  | ||   ||| | ||     |  |   |  |    | | ||||     ||  |||| ||     ||| \+-+-++---------+/ |  | |||| | | |  ||   |||| ||   | |    ||| | |||
|   |||||  |  | ||   ||| | |\-----+--+---+--+----+-+-++++-----++--++++-++-----+++--+-+-++---------+--+--+-++++-+-+-+--++---++++-/|   | |    ||| | |||
|   |||||  |  | ||   ||| | \------+--+---+--+----+-+-++++-----++--++++-++-----+++--+-+-++---------+--+--+-++++-+-+-+--++---+++/  |   | |    ||| | |||
|   |||||  |  | ||   \++-+--------+--+---+--+----+-+-++++-----++--++++-++-----+++--+-+-+/         |  |  | |||| |/+-+--++---+++---+--\| |    ||| | |||
\---+++++--+--+-++----++-+--------+--+---+--+----+-+-++/|     ||  |||| ||     |||  | | |          |  |  | |||| ||| |  ||   |||   |  || |    ||| | |||
    |||||  |  | ||    || \--------+--+---/  |    | | || |     ||  |||| ||     |||  | | \----------+--+--+-++++-+++-+--++---+++---+--++-+----+/| | |||
    |||||  |  | || /--++----------+--+--\   |    | \-++-+-----++--++++-++-----+++--+-+------------+--+--+-++++-+++-+--++---+++---+--++-/    | | | |||
    |||||  |  | || |  ||          |  |  |   \----+---++-+-----++--++++-++-----+++--+-+------------+--/  | |||| ||| |  ||   |||   |  ||      | | | |||
    |||||  |  | || |  ||          |  |  |        |   |\-+-----++--++++-++-----+++--+-+------------+-----+-++++-+++-+--++---++/   |  ||      | | | |||
    |||||  \--+-++-+--++----------+--+--+--------+---+--+-----++--++++-++-----+++--+-+------------+-----+-++++-+++-+--/|   ||    |  ||      | | | |||
    |\+++-----+-++-+--++----------+--+--+--------+---+--+-----++--++++-++-----+++--+-/            |     | |||| ||| |   |   ||    |  ||      | | | |||
    | |||  /--+-++-+--++----------+--+--+--------+---+--+-----++--++++-++-----+++\ |              |     | |||| ||| |   |   ||    |  ||      | | | |||
    | |||  |  | || |  ||          |  |  |        |   |  \-----++--++++-++-----++++-+--------------+-----+-++++-+++-+---+---++----+--++------+-+-+-++/
    | |||  |  | || |/-++----------+--+--+--------+---+----\   ||  |||| ||     |||| |              |     | |||| ||| |   |   ||    v  ||      | | | ||
    | |||  |  | || || ||          |  |  |        \---+----+---++--++++-++-----++++-/              |     | |||| ||| |   |   ||    |  ||      | | | ||
 /--+-+++--+-\| || || ||          |  |  |            |    |   ||  |||| ||     ||||                |     | |||| ||| |   |   ||    |  ||      | | | ||
 |  | |||  | || || \+-++----------+->+--/            |    |   ||  |\++-++-----/|||                |     | |||| ||| |   |   ||    |  ||      | | | ||
 |  | |||  | || ||  | ||          |  |               |    |   ||  | || ||      |||               /+-----+-++++-+++-+---+---++----+--++------+-+-+-++\
 |  | ||\--+-++-++--+-++--->------+--+---------------+----+---++--+-++-++------+++---------------++-----+-/||| ||| |   |   ||    |  ||      | | | |||
 |  | ||   | || ||  | ||          \--+---------------+----+---+/  | || ||      |||               ||     |  ||| |\+-+---+---++----+--/|      | | | |||
 |  | ||   | || ||  | \+-------------+---------------+----+---+---+-++-++------+++---------------++-----+--++/ | | |   |   ||    |   |      | | | |||
 |  | ||   | |\-++--+--+-------------+---------------+----+---+---+-++-/|      |\+---------------++-----+--++--+-+-/   |   ||    |   \------+-/ | |||
 |  | ||   | |  ||  |  |             |               |    |   |   | ||  \------+-+---------------+/     |  ||  | |     |   ||    |          |   | |||
 |  \-++---+-+--++--+--/             \---------------+----+---+---+-+/         | |               |      \--++--+-+-----+---++----+----------+---/ |||
 |    |\---+-+--++--+--------------------------------+----+---+---+-+----------+-+---------------+---------/|  | \-----+---++----+----------+-----/||
 \----+----+-/  ||  |                                \----+---/   | |          | |               |          |  |       |   ||    |          |      ||
      |    |    ||  |                                     |       \-+----------+-+---------------+----------/  |       |   ||    |          |      ||
      |    |    \+--+-------------------------------------+---------+----------+-+---------------+-------------+-------/   \+----+----------+------/|
      |    |     \--+-------------------------------------+---------+----------+-+---------------+-------------+------------/    |          |       |
      |    \--------+-------------------------------------+---------+----------+-/               |             |                 |          |       |
      |             \-------------------------------------/         |          \-----------------+-------------+-----------------/          |       |
      |                                                             |                            |             \----------------------------/       |
      \-------------------------------------------------------------/                            \--------------------------------------------------/ `

val testInput = `/->-\
|   |  /----\
| /-+--+-\  |
| | |  | v  |
\-+-/  \-+--/
  \------/   `

class Cart =

	Var X: Int
	Var Y: Int

	Var Direction = 0
	Var State = 0

	Def new(x: Int, y: Int, direction: Int) =
		X = x
		Y = y
		Direction = direction

	Def toString() = "(" + X + ", " + Y + ")"

class CartComparator: Comparator<Cart> =
	Def Compare(a: Cart, b: Cart) =
		if(a.Y < b.Y) return -1
		a.X - b.X


class Day13 =

	Val Up = 0
	Val Right = 1
	Val Down = 2
	Val Left = 3

	Val StateLeft = 0
	Val StateStraight = 1
	Val StateRight = 2

	var X: Int
	var Y: Int
	val carts = new Vector<Cart>()
	var graph: Char[][]

	Def new(input: String) =
		val lines = input.Lines()
		Y = lines.Size()
		for(val l in lines)
			X = Math.max(X, l.Size())
		graph = new Char[Y][X]
		for(var y = 0; y < Y; y++)
			for(var x = 0; x < X; x++)
				graph[y][x] = x < lines[y].Size() ? lines[y][x] : ' '

		for(var y = 0; y < Y; y++)
			for(var x = 0; x < X; x++)
				val c = graph[y][x]
				if(c == '>')
					graph[y][x] = '-'
					carts.Add(new Cart(x, y, Right))
				else if(c == '<')
					graph[y][x] = '-'
					carts.Add(new Cart(x, y, Left))
				else if(c == '^')
					graph[y][x] = '|'
					carts.Add(new Cart(x, y, Up))
				else if(c == 'v')
					graph[y][x] = '|'
					carts.Add(new Cart(x, y, Down))

	Def PrintGraph() =
		for(var y = 0; y < Y; y++)
			for(var x = 0; x < X; x++)
				var printedCart = false
				for(val cart in carts)
					if(cart.X == x && cart.Y == y)
						PrintCart(cart)
						printedCart = true
						break
				if(!printedCart)
					print(graph[y][x])
			println()

	Def PrintCart(cart: Cart) =
		if(cart.Direction == Up) print("^")
		else if(cart.Direction == Right) print(">")
		else if(cart.Direction == Down) print("v")
		else if(cart.Direction == Left) print("<")


	Def Run() =
		while(true)
			carts.Sort(new CartComparator())
			for(val cart in carts)
				MoveCart(cart)
				for(val cart2 in carts)
					if(cart != cart2 && cart.X == cart2.X && cart.Y == cart2.Y)
						println(cart.X + "," + cart.Y) // res: "111,13"
						return

	Def MoveCart(cart: Cart) =
		val c = graph[cart.Y][cart.X]
		if(c == '|')
			if(cart.Direction == Up)   cart.Y--
			if(cart.Direction == Down) cart.Y++
		else if(c == '-')
			if(cart.Direction == Left)  cart.X--
			if(cart.Direction == Right) cart.X++
		else if(c == '/')
			if(cart.Direction == Up)
				cart.X++
				cart.Direction = Right
			else if(cart.Direction == Left)
				cart.Y++
				cart.Direction = Down
			else if(cart.Direction == Right)
				cart.Y--
				cart.Direction = Up
			else if(cart.Direction == Down)
				cart.X--
				cart.Direction = Left
		else if(c == '\\')
			if(cart.Direction == Up)
				cart.X--
				cart.Direction = Left
			else if(cart.Direction == Left)
				cart.Y--
				cart.Direction = Up
			else if(cart.Direction == Right)
				cart.Y++
				cart.Direction = Down
			else if(cart.Direction == Down)
				cart.X++
				cart.Direction = Right
		else if(c == '+')
			if(cart.Direction == Up)
				if(cart.State == StateLeft)
					cart.X--
					cart.Direction = Left
				if(cart.State == StateStraight) cart.Y--
				if(cart.State == StateRight)
					cart.X++
					cart.Direction = Right
			else if(cart.Direction == Right)
				if(cart.State == StateLeft)
					cart.Y--
					cart.Direction = Up
				if(cart.State == StateStraight) cart.X++
				if(cart.State == StateRight)
					cart.Y++
					cart.Direction = Down
			else if(cart.Direction == Down)
				if(cart.State == StateLeft)
					cart.X++
					cart.Direction = Right
				if(cart.State == StateStraight)
					cart.Y++
				if(cart.State == StateRight)
					cart.X--
					cart.Direction = Left
			else if(cart.Direction == Left)
				if(cart.State == StateLeft)
					cart.Y++
					cart.Direction = Down
				if(cart.State == StateStraight) cart.X--
				if(cart.State == StateRight)
					cart.Y--
					cart.Direction = Up
			cart.State = (cart.State + 1) % 3

new Day13(input).Run()
