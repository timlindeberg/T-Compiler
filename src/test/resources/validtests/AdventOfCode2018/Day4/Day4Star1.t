import T::std::Vector
import T::std::HashMap
import T::std::Comparator
import java::util::regex::Matcher
import java::util::regex::Pattern
import java::text::SimpleDateFormat
import java::util::Date

val input =`[1518-05-31 00:27] falls asleep
[1518-04-15 00:54] wakes up
[1518-04-06 00:42] falls asleep
[1518-02-08 00:41] wakes up
[1518-11-11 00:59] wakes up
[1518-11-09 00:46] wakes up
[1518-06-15 00:29] falls asleep
[1518-05-25 00:44] wakes up
[1518-11-21 00:00] Guard #3203 begins shift
[1518-08-28 00:04] Guard #1163 begins shift
[1518-11-07 00:36] wakes up
[1518-04-22 00:16] falls asleep
[1518-07-19 00:31] falls asleep
[1518-05-04 23:48] Guard #1663 begins shift
[1518-10-31 00:24] falls asleep
[1518-11-07 00:54] wakes up
[1518-06-18 00:04] Guard #859 begins shift
[1518-06-26 00:38] falls asleep
[1518-07-12 00:46] wakes up
[1518-09-14 00:15] wakes up
[1518-09-03 00:47] wakes up
[1518-05-26 00:41] wakes up
[1518-03-17 00:56] wakes up
[1518-10-25 00:47] falls asleep
[1518-04-08 00:34] falls asleep
[1518-05-10 00:44] falls asleep
[1518-04-17 00:50] wakes up
[1518-06-29 00:39] falls asleep
[1518-04-19 00:59] wakes up
[1518-11-07 00:35] falls asleep
[1518-06-18 00:07] falls asleep
[1518-09-26 00:30] falls asleep
[1518-10-12 00:31] falls asleep
[1518-09-10 00:59] wakes up
[1518-07-27 00:53] wakes up
[1518-08-05 00:00] falls asleep
[1518-10-28 00:53] wakes up
[1518-05-19 00:00] Guard #997 begins shift
[1518-06-27 00:50] wakes up
[1518-10-17 00:28] falls asleep
[1518-05-11 00:45] wakes up
[1518-03-18 00:07] falls asleep
[1518-04-03 00:50] wakes up
[1518-04-21 00:04] Guard #113 begins shift
[1518-07-24 00:50] wakes up
[1518-09-13 00:41] falls asleep
[1518-06-19 00:30] wakes up
[1518-09-14 00:43] falls asleep
[1518-06-24 00:56] wakes up
[1518-07-12 00:28] falls asleep
[1518-04-21 00:33] wakes up
[1518-08-31 00:00] Guard #739 begins shift
[1518-05-15 00:55] wakes up
[1518-06-09 00:39] falls asleep
[1518-03-12 00:03] Guard #997 begins shift
[1518-04-15 00:16] falls asleep
[1518-09-07 00:26] falls asleep
[1518-03-26 00:21] wakes up
[1518-06-26 00:40] wakes up
[1518-02-05 00:02] Guard #1663 begins shift
[1518-04-13 00:06] falls asleep
[1518-04-24 23:59] Guard #733 begins shift
[1518-02-09 23:59] Guard #419 begins shift
[1518-08-28 00:58] wakes up
[1518-03-16 00:58] wakes up
[1518-11-13 00:19] falls asleep
[1518-03-13 00:54] wakes up
[1518-04-12 00:46] falls asleep
[1518-03-04 00:17] falls asleep
[1518-04-10 00:40] falls asleep
[1518-05-11 00:01] Guard #1367 begins shift
[1518-04-09 00:42] wakes up
[1518-03-29 00:53] wakes up
[1518-02-27 00:01] Guard #859 begins shift
[1518-08-13 00:52] falls asleep
[1518-09-22 00:43] falls asleep
[1518-05-13 00:19] falls asleep
[1518-05-14 00:06] falls asleep
[1518-08-05 00:18] wakes up
[1518-11-10 00:41] falls asleep
[1518-09-10 00:43] falls asleep
[1518-03-02 00:44] falls asleep
[1518-04-16 23:57] Guard #1163 begins shift
[1518-09-09 00:43] falls asleep
[1518-02-09 00:01] Guard #997 begins shift
[1518-09-13 00:35] falls asleep
[1518-09-08 00:04] Guard #3407 begins shift
[1518-08-02 00:14] falls asleep
[1518-11-07 00:02] Guard #2113 begins shift
[1518-06-13 23:57] Guard #661 begins shift
[1518-04-29 00:57] wakes up
[1518-11-04 00:56] falls asleep
[1518-08-03 00:54] wakes up
[1518-06-29 00:17] falls asleep
[1518-07-15 00:37] wakes up
[1518-06-14 23:58] Guard #2221 begins shift
[1518-02-11 00:41] falls asleep
[1518-10-24 23:56] Guard #739 begins shift
[1518-06-12 23:56] Guard #1663 begins shift
[1518-02-23 23:51] Guard #2113 begins shift
[1518-05-09 00:24] falls asleep
[1518-09-21 00:22] wakes up
[1518-02-11 00:38] wakes up
[1518-07-23 00:54] falls asleep
[1518-03-10 23:59] Guard #661 begins shift
[1518-03-03 00:03] falls asleep
[1518-06-08 00:41] wakes up
[1518-02-05 00:12] falls asleep
[1518-09-23 00:51] wakes up
[1518-05-02 00:39] wakes up
[1518-07-06 00:06] falls asleep
[1518-03-27 23:57] Guard #2221 begins shift
[1518-04-29 00:00] Guard #661 begins shift
[1518-05-05 00:22] wakes up
[1518-09-12 00:38] wakes up
[1518-04-28 00:48] falls asleep
[1518-10-08 00:58] wakes up
[1518-08-22 00:55] falls asleep
[1518-10-22 00:52] falls asleep
[1518-07-15 00:56] wakes up
[1518-08-15 23:57] Guard #859 begins shift
[1518-05-19 23:53] Guard #3407 begins shift
[1518-11-03 00:03] Guard #2713 begins shift
[1518-10-02 00:01] Guard #859 begins shift
[1518-03-08 00:13] falls asleep
[1518-03-15 00:02] Guard #859 begins shift
[1518-05-01 00:55] wakes up
[1518-07-25 00:41] falls asleep
[1518-05-24 00:24] falls asleep
[1518-05-03 00:58] wakes up
[1518-04-11 00:32] falls asleep
[1518-09-24 23:54] Guard #2383 begins shift
[1518-10-24 00:23] falls asleep
[1518-06-24 00:28] falls asleep
[1518-08-04 00:59] wakes up
[1518-10-21 00:19] wakes up
[1518-03-21 00:04] Guard #661 begins shift
[1518-06-18 00:15] wakes up
[1518-09-29 00:09] falls asleep
[1518-05-26 00:20] falls asleep
[1518-10-20 00:04] falls asleep
[1518-09-29 00:19] wakes up
[1518-08-10 00:03] falls asleep
[1518-08-03 00:03] falls asleep
[1518-07-14 00:41] falls asleep
[1518-09-26 00:13] wakes up
[1518-02-21 00:05] falls asleep
[1518-08-21 00:14] falls asleep
[1518-04-27 00:46] wakes up
[1518-06-11 23:57] Guard #3203 begins shift
[1518-04-26 00:18] wakes up
[1518-02-24 00:02] falls asleep
[1518-09-13 00:21] falls asleep
[1518-07-06 00:18] wakes up
[1518-08-08 00:38] falls asleep
[1518-03-23 00:37] falls asleep
[1518-08-05 00:32] falls asleep
[1518-05-12 00:51] falls asleep
[1518-05-28 00:58] wakes up
[1518-02-15 00:24] wakes up
[1518-02-17 00:30] falls asleep
[1518-08-11 00:58] wakes up
[1518-04-26 00:09] falls asleep
[1518-06-20 00:58] wakes up
[1518-08-09 00:36] falls asleep
[1518-04-25 00:19] wakes up
[1518-02-28 00:58] wakes up
[1518-04-18 00:06] falls asleep
[1518-03-08 00:53] wakes up
[1518-09-13 00:36] wakes up
[1518-04-10 00:01] Guard #3391 begins shift
[1518-11-15 00:48] wakes up
[1518-09-24 00:43] wakes up
[1518-05-15 00:14] wakes up
[1518-07-14 00:59] wakes up
[1518-06-28 23:57] Guard #733 begins shift
[1518-09-13 00:25] wakes up
[1518-07-26 23:56] Guard #3203 begins shift
[1518-05-28 00:47] wakes up
[1518-11-09 00:03] Guard #3391 begins shift
[1518-07-01 00:52] wakes up
[1518-10-07 00:04] Guard #661 begins shift
[1518-11-05 00:59] wakes up
[1518-06-15 00:45] wakes up
[1518-09-02 00:40] wakes up
[1518-06-05 00:00] Guard #419 begins shift
[1518-02-04 00:57] wakes up
[1518-07-10 00:30] falls asleep
[1518-05-14 00:04] Guard #61 begins shift
[1518-06-06 00:00] Guard #3203 begins shift
[1518-11-22 00:04] Guard #2713 begins shift
[1518-02-11 00:55] wakes up
[1518-08-15 00:47] wakes up
[1518-10-03 00:02] Guard #2383 begins shift
[1518-06-02 00:57] wakes up
[1518-04-01 00:37] wakes up
[1518-06-29 23:58] Guard #859 begins shift
[1518-11-22 00:38] falls asleep
[1518-04-13 00:00] Guard #2609 begins shift
[1518-07-07 00:48] wakes up
[1518-10-29 00:54] wakes up
[1518-02-26 00:59] wakes up
[1518-10-18 00:04] Guard #1367 begins shift
[1518-04-11 00:58] wakes up
[1518-10-27 23:59] Guard #1663 begins shift
[1518-09-12 00:11] falls asleep
[1518-06-21 00:55] wakes up
[1518-06-16 00:00] Guard #2113 begins shift
[1518-05-19 00:48] wakes up
[1518-08-31 00:23] wakes up
[1518-10-14 00:59] wakes up
[1518-02-17 00:04] Guard #2221 begins shift
[1518-11-16 00:58] wakes up
[1518-07-08 00:31] wakes up
[1518-05-04 00:46] falls asleep
[1518-08-02 00:57] wakes up
[1518-03-25 00:00] Guard #3407 begins shift
[1518-07-29 00:37] falls asleep
[1518-07-10 00:04] Guard #3203 begins shift
[1518-07-26 00:32] wakes up
[1518-10-19 00:57] falls asleep
[1518-10-22 23:50] Guard #113 begins shift
[1518-11-05 00:02] Guard #661 begins shift
[1518-03-20 00:38] falls asleep
[1518-05-24 00:51] wakes up
[1518-03-07 00:32] wakes up
[1518-10-03 00:45] falls asleep
[1518-08-15 00:01] Guard #859 begins shift
[1518-10-29 00:03] Guard #2221 begins shift
[1518-11-12 00:00] falls asleep
[1518-11-22 23:58] Guard #733 begins shift
[1518-06-18 00:42] falls asleep
[1518-10-18 00:58] wakes up
[1518-02-19 00:00] Guard #419 begins shift
[1518-03-26 00:19] falls asleep
[1518-03-14 00:49] falls asleep
[1518-09-05 23:47] Guard #419 begins shift
[1518-09-03 00:57] falls asleep
[1518-08-06 00:00] Guard #661 begins shift
[1518-09-24 00:41] falls asleep
[1518-09-15 00:55] wakes up
[1518-10-15 00:50] wakes up
[1518-05-28 00:35] falls asleep
[1518-02-11 00:32] falls asleep
[1518-06-19 00:36] falls asleep
[1518-02-25 00:06] falls asleep
[1518-04-20 00:50] wakes up
[1518-02-17 00:54] wakes up
[1518-05-29 00:00] Guard #2609 begins shift
[1518-07-26 00:00] Guard #2383 begins shift
[1518-08-14 00:11] falls asleep
[1518-08-01 00:00] Guard #2297 begins shift
[1518-04-04 00:41] wakes up
[1518-06-10 00:55] wakes up
[1518-03-19 00:05] falls asleep
[1518-06-11 00:30] falls asleep
[1518-03-31 00:20] falls asleep
[1518-07-13 00:52] wakes up
[1518-08-29 00:11] falls asleep
[1518-05-16 00:42] wakes up
[1518-11-05 00:36] wakes up
[1518-10-22 00:40] wakes up
[1518-09-06 00:01] falls asleep
[1518-09-20 00:42] falls asleep
[1518-11-07 00:48] falls asleep
[1518-07-31 00:59] wakes up
[1518-04-19 00:30] wakes up
[1518-05-12 23:56] Guard #997 begins shift
[1518-03-04 00:53] wakes up
[1518-10-07 00:06] falls asleep
[1518-08-28 00:27] falls asleep
[1518-05-16 00:17] falls asleep
[1518-10-23 00:04] falls asleep
[1518-07-08 00:57] wakes up
[1518-06-14 00:50] wakes up
[1518-04-13 00:46] wakes up
[1518-04-05 23:50] Guard #419 begins shift
[1518-10-25 00:43] wakes up
[1518-04-08 00:18] wakes up
[1518-05-12 00:29] wakes up
[1518-03-28 23:56] Guard #2609 begins shift
[1518-09-13 00:56] wakes up
[1518-03-08 00:48] falls asleep
[1518-02-12 00:32] falls asleep
[1518-10-15 00:02] Guard #2297 begins shift
[1518-07-26 00:38] falls asleep
[1518-04-06 23:47] Guard #2383 begins shift
[1518-10-03 00:40] wakes up
[1518-07-23 00:58] wakes up
[1518-03-17 00:02] Guard #1367 begins shift
[1518-06-30 00:47] falls asleep
[1518-09-06 00:24] falls asleep
[1518-05-04 00:54] wakes up
[1518-04-19 00:39] wakes up
[1518-07-15 00:10] falls asleep
[1518-07-26 00:28] falls asleep
[1518-07-21 00:24] falls asleep
[1518-08-09 23:54] Guard #739 begins shift
[1518-02-21 00:45] falls asleep
[1518-03-10 00:53] wakes up
[1518-06-13 00:09] falls asleep
[1518-08-06 00:47] falls asleep
[1518-10-31 00:53] wakes up
[1518-08-23 23:51] Guard #739 begins shift
[1518-03-13 00:03] Guard #3203 begins shift
[1518-06-02 00:14] falls asleep
[1518-10-20 00:38] falls asleep
[1518-08-25 00:43] falls asleep
[1518-08-31 23:53] Guard #733 begins shift
[1518-04-30 00:17] falls asleep
[1518-08-06 00:12] falls asleep
[1518-11-06 00:02] Guard #739 begins shift
[1518-07-02 00:56] wakes up
[1518-05-29 00:18] falls asleep
[1518-02-14 00:57] wakes up
[1518-03-13 00:52] falls asleep
[1518-09-26 23:49] Guard #2713 begins shift
[1518-05-19 00:37] falls asleep
[1518-08-30 00:26] falls asleep
[1518-05-30 00:08] falls asleep
[1518-09-01 00:54] wakes up
[1518-11-04 00:47] wakes up
[1518-06-04 00:26] wakes up
[1518-08-10 00:48] falls asleep
[1518-04-11 00:02] Guard #2221 begins shift
[1518-07-11 23:57] Guard #61 begins shift
[1518-07-20 00:45] wakes up
[1518-04-03 23:59] Guard #3391 begins shift
[1518-11-08 00:56] wakes up
[1518-08-04 00:56] falls asleep
[1518-09-19 00:43] wakes up
[1518-02-12 00:00] Guard #61 begins shift
[1518-07-06 23:50] Guard #661 begins shift
[1518-06-07 00:20] falls asleep
[1518-08-08 00:02] Guard #997 begins shift
[1518-07-21 00:47] wakes up
[1518-11-15 00:02] Guard #1163 begins shift
[1518-04-30 00:58] wakes up
[1518-08-06 00:19] wakes up
[1518-03-08 23:50] Guard #61 begins shift
[1518-07-08 23:59] Guard #2609 begins shift
[1518-08-22 00:01] Guard #2383 begins shift
[1518-06-30 00:55] wakes up
[1518-06-19 00:25] falls asleep
[1518-05-05 00:03] falls asleep
[1518-09-12 23:56] Guard #2609 begins shift
[1518-07-03 00:43] wakes up
[1518-07-18 00:25] wakes up
[1518-07-16 23:57] Guard #941 begins shift
[1518-11-21 00:20] falls asleep
[1518-11-04 00:59] wakes up
[1518-07-27 00:31] falls asleep
[1518-10-12 00:43] falls asleep
[1518-03-22 00:04] Guard #997 begins shift
[1518-03-05 00:03] Guard #1663 begins shift
[1518-06-27 00:53] falls asleep
[1518-08-22 00:45] falls asleep
[1518-07-08 00:00] Guard #1367 begins shift
[1518-06-01 00:14] falls asleep
[1518-02-25 00:01] Guard #1163 begins shift
[1518-09-22 00:15] falls asleep
[1518-05-03 00:03] falls asleep
[1518-09-22 00:02] falls asleep
[1518-09-21 00:54] wakes up
[1518-06-21 00:00] Guard #859 begins shift
[1518-06-28 00:59] wakes up
[1518-03-19 00:21] wakes up
[1518-08-31 00:38] wakes up
[1518-09-26 00:49] wakes up
[1518-11-19 00:36] wakes up
[1518-02-28 00:02] Guard #3391 begins shift
[1518-02-12 00:58] wakes up
[1518-11-08 00:31] falls asleep
[1518-04-17 00:41] falls asleep
[1518-10-11 00:02] Guard #2113 begins shift
[1518-08-03 00:41] falls asleep
[1518-06-13 00:43] falls asleep
[1518-03-28 00:50] falls asleep
[1518-10-21 00:53] wakes up
[1518-04-21 00:48] falls asleep
[1518-02-16 00:56] wakes up
[1518-03-09 00:27] falls asleep
[1518-08-04 00:13] falls asleep
[1518-02-17 00:47] falls asleep
[1518-06-10 23:57] Guard #1163 begins shift
[1518-10-11 00:49] falls asleep
[1518-08-01 00:17] wakes up
[1518-02-20 23:52] Guard #3407 begins shift
[1518-03-09 00:00] falls asleep
[1518-10-21 23:46] Guard #661 begins shift
[1518-02-19 00:29] falls asleep
[1518-08-23 00:36] falls asleep
[1518-06-16 00:11] falls asleep
[1518-08-26 00:52] wakes up
[1518-08-07 00:43] falls asleep
[1518-11-07 23:59] Guard #997 begins shift
[1518-11-18 00:10] falls asleep
[1518-07-23 00:34] falls asleep
[1518-03-30 00:59] wakes up
[1518-03-29 00:48] falls asleep
[1518-09-20 23:50] Guard #419 begins shift
[1518-06-06 23:57] Guard #1663 begins shift
[1518-08-13 00:00] Guard #2113 begins shift
[1518-04-28 00:04] Guard #739 begins shift
[1518-09-11 00:34] falls asleep
[1518-06-18 00:28] falls asleep
[1518-05-21 00:07] falls asleep
[1518-08-25 00:40] wakes up
[1518-08-20 00:48] falls asleep
[1518-09-30 00:00] Guard #3391 begins shift
[1518-08-02 00:56] falls asleep
[1518-08-13 00:57] wakes up
[1518-05-23 00:00] Guard #739 begins shift
[1518-05-04 00:38] wakes up
[1518-08-04 00:45] wakes up
[1518-02-15 00:00] Guard #3391 begins shift
[1518-08-13 23:58] Guard #1663 begins shift
[1518-06-04 00:55] wakes up
[1518-04-08 00:04] falls asleep
[1518-06-28 00:57] falls asleep
[1518-08-09 00:44] wakes up
[1518-06-25 00:38] wakes up
[1518-03-03 00:48] falls asleep
[1518-05-02 00:43] falls asleep
[1518-10-10 00:51] falls asleep
[1518-11-04 00:43] falls asleep
[1518-08-10 00:45] wakes up
[1518-09-27 00:58] wakes up
[1518-04-17 23:58] Guard #3203 begins shift
[1518-07-04 00:34] falls asleep
[1518-03-14 00:42] wakes up
[1518-10-12 00:39] wakes up
[1518-08-16 00:49] wakes up
[1518-10-19 23:54] Guard #997 begins shift
[1518-05-17 00:00] Guard #2383 begins shift
[1518-03-03 00:54] wakes up
[1518-09-27 00:57] falls asleep
[1518-03-30 00:41] falls asleep
[1518-11-06 00:57] wakes up
[1518-09-14 00:00] falls asleep
[1518-09-28 00:16] falls asleep
[1518-09-14 00:44] wakes up
[1518-05-08 23:58] Guard #1163 begins shift
[1518-09-12 00:02] Guard #859 begins shift
[1518-08-11 00:15] falls asleep
[1518-10-09 23:56] Guard #2221 begins shift
[1518-05-01 23:53] Guard #733 begins shift
[1518-07-19 00:52] wakes up
[1518-11-22 00:59] wakes up
[1518-11-23 00:28] falls asleep
[1518-08-15 00:37] falls asleep
[1518-05-02 00:52] wakes up
[1518-06-25 23:58] Guard #2609 begins shift
[1518-07-06 00:16] falls asleep
[1518-05-30 00:03] Guard #997 begins shift
[1518-03-06 00:20] falls asleep
[1518-09-22 00:51] wakes up
[1518-11-08 00:42] wakes up
[1518-07-24 23:56] Guard #739 begins shift
[1518-06-11 00:37] wakes up
[1518-04-15 00:49] falls asleep
[1518-07-26 00:57] wakes up
[1518-02-19 23:50] Guard #2221 begins shift
[1518-05-03 00:29] wakes up
[1518-04-15 00:36] wakes up
[1518-03-26 00:04] Guard #1163 begins shift
[1518-10-18 00:15] falls asleep
[1518-04-23 23:59] Guard #3391 begins shift
[1518-06-28 00:35] wakes up
[1518-05-04 00:37] falls asleep
[1518-10-14 00:00] Guard #997 begins shift
[1518-07-18 00:44] wakes up
[1518-04-03 00:04] falls asleep
[1518-04-13 00:36] wakes up
[1518-06-09 00:01] Guard #739 begins shift
[1518-03-13 00:38] falls asleep
[1518-06-08 00:57] falls asleep
[1518-04-21 00:10] falls asleep
[1518-08-04 00:04] Guard #859 begins shift
[1518-03-02 00:58] wakes up
[1518-06-15 00:34] wakes up
[1518-02-06 00:50] wakes up
[1518-11-20 00:43] wakes up
[1518-11-15 23:57] Guard #859 begins shift
[1518-07-26 00:23] wakes up
[1518-05-24 00:02] Guard #3391 begins shift
[1518-07-18 00:22] falls asleep
[1518-04-25 00:31] falls asleep
[1518-08-27 00:49] falls asleep
[1518-07-21 23:58] Guard #349 begins shift
[1518-06-10 00:54] falls asleep
[1518-09-17 00:04] Guard #733 begins shift
[1518-06-20 00:39] falls asleep
[1518-03-23 00:01] Guard #2297 begins shift
[1518-05-21 00:59] wakes up
[1518-09-24 00:17] falls asleep
[1518-06-23 00:12] falls asleep
[1518-05-20 00:03] falls asleep
[1518-09-26 00:03] falls asleep
[1518-05-31 00:37] falls asleep
[1518-06-30 23:59] Guard #739 begins shift
[1518-07-07 00:05] falls asleep
[1518-04-19 00:17] falls asleep
[1518-03-13 00:44] wakes up
[1518-09-03 00:00] Guard #661 begins shift
[1518-02-15 23:49] Guard #739 begins shift
[1518-04-07 00:05] falls asleep
[1518-02-26 00:01] Guard #661 begins shift
[1518-11-17 23:57] Guard #859 begins shift
[1518-08-22 00:47] wakes up
[1518-09-23 00:16] falls asleep
[1518-07-21 00:40] falls asleep
[1518-03-31 00:00] Guard #1663 begins shift
[1518-08-01 00:10] falls asleep
[1518-09-30 00:52] wakes up
[1518-04-14 00:08] falls asleep
[1518-02-06 23:56] Guard #859 begins shift
[1518-04-16 00:27] wakes up
[1518-05-22 00:29] falls asleep
[1518-11-01 00:31] falls asleep
[1518-09-28 00:39] wakes up
[1518-05-22 00:30] wakes up
[1518-10-03 00:55] wakes up
[1518-06-21 23:56] Guard #907 begins shift
[1518-05-28 00:57] falls asleep
[1518-06-06 00:40] falls asleep
[1518-08-23 00:59] wakes up
[1518-05-20 00:47] wakes up
[1518-03-12 00:31] falls asleep
[1518-07-12 00:56] wakes up
[1518-08-03 00:25] wakes up
[1518-05-08 00:04] Guard #419 begins shift
[1518-10-09 00:00] Guard #419 begins shift
[1518-10-30 00:04] Guard #733 begins shift
[1518-02-09 00:37] falls asleep
[1518-03-17 23:59] Guard #2297 begins shift
[1518-02-21 00:08] wakes up
[1518-05-10 00:47] wakes up
[1518-11-20 00:01] falls asleep
[1518-07-06 00:02] Guard #113 begins shift
[1518-06-14 00:34] falls asleep
[1518-09-11 00:39] wakes up
[1518-05-27 00:27] wakes up
[1518-04-07 00:36] wakes up
[1518-03-27 00:03] Guard #907 begins shift
[1518-07-18 00:42] falls asleep
[1518-11-17 00:00] Guard #3407 begins shift
[1518-04-11 00:06] falls asleep
[1518-06-07 00:59] wakes up
[1518-05-04 00:01] Guard #1663 begins shift
[1518-05-03 00:47] wakes up
[1518-10-06 00:51] wakes up
[1518-03-12 00:57] wakes up
[1518-03-23 00:23] falls asleep
[1518-03-02 23:48] Guard #3391 begins shift
[1518-05-14 23:52] Guard #733 begins shift
[1518-08-15 00:55] wakes up
[1518-08-14 00:54] wakes up
[1518-06-08 00:30] falls asleep
[1518-02-18 00:58] wakes up
[1518-11-03 00:58] wakes up
[1518-06-17 00:00] Guard #2221 begins shift
[1518-04-05 00:14] falls asleep
[1518-08-29 00:01] falls asleep
[1518-05-14 00:13] wakes up
[1518-10-25 00:50] wakes up
[1518-06-24 00:03] Guard #739 begins shift
[1518-10-26 00:54] wakes up
[1518-08-17 00:14] falls asleep
[1518-10-04 00:03] Guard #3407 begins shift
[1518-06-05 00:30] wakes up
[1518-06-09 23:59] Guard #2221 begins shift
[1518-09-19 00:59] wakes up
[1518-10-05 23:56] Guard #3203 begins shift
[1518-05-02 00:03] falls asleep
[1518-09-25 00:03] falls asleep
[1518-09-11 00:42] falls asleep
[1518-07-08 00:08] falls asleep
[1518-05-07 00:58] wakes up
[1518-07-24 00:58] wakes up
[1518-04-12 00:21] wakes up
[1518-10-29 00:29] falls asleep
[1518-07-28 23:56] Guard #3391 begins shift
[1518-07-16 00:23] falls asleep
[1518-06-04 00:00] Guard #2713 begins shift
[1518-08-29 00:07] wakes up
[1518-08-31 00:46] falls asleep
[1518-03-14 00:54] wakes up
[1518-09-29 00:04] Guard #2383 begins shift
[1518-08-07 00:56] wakes up
[1518-06-02 00:56] falls asleep
[1518-03-12 00:19] wakes up
[1518-04-28 00:52] wakes up
[1518-07-13 00:58] wakes up
[1518-04-08 23:57] Guard #3203 begins shift
[1518-04-05 00:49] wakes up
[1518-07-03 00:22] falls asleep
[1518-02-12 00:53] wakes up
[1518-09-24 00:03] Guard #733 begins shift
[1518-07-30 00:01] Guard #1163 begins shift
[1518-03-16 00:00] Guard #739 begins shift
[1518-08-18 00:50] wakes up
[1518-03-22 00:57] wakes up
[1518-06-02 00:04] Guard #3391 begins shift
[1518-09-01 00:02] falls asleep
[1518-09-11 00:49] wakes up
[1518-04-13 00:41] falls asleep
[1518-04-18 00:53] wakes up
[1518-03-21 00:38] wakes up
[1518-06-02 00:52] wakes up
[1518-07-18 23:59] Guard #733 begins shift
[1518-06-25 00:59] wakes up
[1518-04-07 23:54] Guard #2713 begins shift
[1518-06-28 00:14] falls asleep
[1518-03-04 00:04] Guard #1663 begins shift
[1518-05-27 00:04] falls asleep
[1518-10-08 00:31] falls asleep
[1518-11-05 00:16] falls asleep
[1518-05-14 00:53] falls asleep
[1518-10-04 00:47] wakes up
[1518-09-07 00:11] falls asleep
[1518-09-25 23:48] Guard #2297 begins shift
[1518-10-26 00:00] Guard #1663 begins shift
[1518-03-11 00:56] wakes up
[1518-08-28 23:49] Guard #1663 begins shift
[1518-08-31 00:47] wakes up
[1518-09-04 00:52] falls asleep
[1518-11-16 00:38] falls asleep
[1518-10-19 00:59] wakes up
[1518-10-19 00:23] falls asleep
[1518-05-01 00:23] falls asleep
[1518-05-15 23:56] Guard #3391 begins shift
[1518-07-01 00:07] falls asleep
[1518-07-30 00:52] falls asleep
[1518-05-12 00:27] falls asleep
[1518-04-04 00:07] falls asleep
[1518-06-09 00:58] wakes up
[1518-02-28 00:16] falls asleep
[1518-09-05 00:30] falls asleep
[1518-05-06 00:52] falls asleep
[1518-10-21 00:02] Guard #733 begins shift
[1518-07-30 00:56] wakes up
[1518-09-30 00:13] falls asleep
[1518-10-16 23:58] Guard #997 begins shift
[1518-05-26 00:03] Guard #61 begins shift
[1518-06-08 00:25] wakes up
[1518-07-11 00:01] Guard #1367 begins shift
[1518-07-15 00:02] Guard #61 begins shift
[1518-10-26 00:50] falls asleep
[1518-05-26 00:45] falls asleep
[1518-07-16 00:00] Guard #739 begins shift
[1518-09-18 00:04] Guard #3407 begins shift
[1518-03-22 00:53] falls asleep
[1518-06-25 00:16] falls asleep
[1518-06-27 23:59] Guard #2609 begins shift
[1518-09-16 00:41] falls asleep
[1518-08-20 00:58] wakes up
[1518-02-13 00:02] Guard #3407 begins shift
[1518-02-23 00:50] wakes up
[1518-05-28 00:00] Guard #61 begins shift
[1518-04-06 00:39] wakes up
[1518-11-21 00:42] wakes up
[1518-07-06 00:12] wakes up
[1518-08-11 00:00] Guard #2297 begins shift
[1518-06-04 00:18] falls asleep
[1518-09-07 00:49] wakes up
[1518-06-27 00:56] wakes up
[1518-09-25 00:38] wakes up
[1518-04-06 00:19] wakes up
[1518-09-21 00:02] falls asleep
[1518-03-15 00:38] wakes up
[1518-11-10 00:00] Guard #2297 begins shift
[1518-08-31 00:14] falls asleep
[1518-02-09 00:38] wakes up
[1518-10-22 00:01] falls asleep
[1518-06-11 00:52] wakes up
[1518-04-20 00:39] falls asleep
[1518-04-08 00:57] wakes up
[1518-05-17 23:50] Guard #733 begins shift
[1518-02-10 00:47] wakes up
[1518-09-23 00:00] Guard #1163 begins shift
[1518-10-04 00:22] falls asleep
[1518-02-08 00:09] falls asleep
[1518-10-16 00:44] falls asleep
[1518-07-01 23:59] Guard #1367 begins shift
[1518-10-16 00:50] wakes up
[1518-05-15 00:17] falls asleep
[1518-07-30 00:40] wakes up
[1518-10-16 00:28] falls asleep
[1518-04-06 00:37] falls asleep
[1518-03-08 00:23] wakes up
[1518-06-04 00:54] falls asleep
[1518-09-12 00:45] wakes up
[1518-03-15 00:12] falls asleep
[1518-05-23 00:22] falls asleep
[1518-07-28 00:00] Guard #907 begins shift
[1518-08-22 00:56] wakes up
[1518-08-24 00:33] falls asleep
[1518-04-22 00:00] Guard #739 begins shift
[1518-02-13 23:57] Guard #1367 begins shift
[1518-07-09 00:49] falls asleep
[1518-10-09 00:20] falls asleep
[1518-04-01 23:56] Guard #941 begins shift
[1518-10-16 00:00] Guard #113 begins shift
[1518-09-21 00:34] falls asleep
[1518-07-23 00:35] wakes up
[1518-03-01 00:12] falls asleep
[1518-02-03 23:50] Guard #661 begins shift
[1518-09-09 00:56] wakes up
[1518-03-07 00:22] falls asleep
[1518-08-21 00:53] wakes up
[1518-09-04 00:01] Guard #2297 begins shift
[1518-08-10 00:58] wakes up
[1518-07-03 23:56] Guard #3407 begins shift
[1518-08-26 00:42] falls asleep
[1518-11-13 23:56] Guard #907 begins shift
[1518-10-15 00:16] falls asleep
[1518-08-16 00:44] falls asleep
[1518-07-18 00:01] Guard #2609 begins shift
[1518-10-20 00:22] wakes up
[1518-10-24 00:01] Guard #2609 begins shift
[1518-04-28 00:55] falls asleep
[1518-02-11 00:00] Guard #1367 begins shift
[1518-06-19 00:39] wakes up
[1518-09-27 00:04] falls asleep
[1518-04-19 00:46] falls asleep
[1518-08-10 00:31] falls asleep
[1518-09-21 23:48] Guard #2713 begins shift
[1518-09-10 23:57] Guard #997 begins shift
[1518-10-13 00:55] falls asleep
[1518-08-20 23:59] Guard #3203 begins shift
[1518-06-27 00:33] falls asleep
[1518-05-10 00:00] Guard #739 begins shift
[1518-08-30 00:00] Guard #2609 begins shift
[1518-09-05 00:24] wakes up
[1518-09-11 00:58] wakes up
[1518-07-08 00:39] falls asleep
[1518-08-22 00:20] wakes up
[1518-07-29 00:43] wakes up
[1518-08-22 00:14] falls asleep
[1518-08-02 00:53] wakes up
[1518-11-17 00:24] falls asleep
[1518-03-25 00:11] falls asleep
[1518-03-06 00:48] wakes up
[1518-09-12 00:43] falls asleep
[1518-10-20 00:57] wakes up
[1518-04-04 23:57] Guard #733 begins shift
[1518-09-03 00:58] wakes up
[1518-09-16 00:59] wakes up
[1518-07-23 00:00] Guard #2113 begins shift
[1518-09-04 00:59] wakes up
[1518-09-11 00:53] falls asleep
[1518-04-08 00:54] falls asleep
[1518-08-07 00:03] Guard #859 begins shift
[1518-08-04 23:49] Guard #2297 begins shift
[1518-05-23 00:51] wakes up
[1518-11-01 23:58] Guard #907 begins shift
[1518-02-05 23:56] Guard #419 begins shift
[1518-08-17 00:01] Guard #3391 begins shift
[1518-09-27 23:58] Guard #3391 begins shift
[1518-03-16 00:35] falls asleep
[1518-08-17 23:57] Guard #113 begins shift
[1518-02-07 00:44] wakes up
[1518-08-18 00:24] falls asleep
[1518-11-01 00:00] Guard #661 begins shift
[1518-10-19 00:39] wakes up
[1518-08-24 23:59] Guard #997 begins shift
[1518-05-18 00:04] falls asleep
[1518-03-17 00:30] falls asleep
[1518-04-08 00:51] wakes up
[1518-08-24 00:53] wakes up
[1518-09-22 00:03] wakes up
[1518-06-08 00:59] wakes up
[1518-04-10 00:56] wakes up
[1518-05-22 00:01] Guard #2383 begins shift
[1518-06-01 00:00] Guard #661 begins shift
[1518-08-22 23:58] Guard #997 begins shift
[1518-03-22 00:42] wakes up
[1518-10-21 00:51] falls asleep
[1518-11-11 23:48] Guard #997 begins shift
[1518-05-01 00:00] Guard #1163 begins shift
[1518-05-20 23:56] Guard #3203 begins shift
[1518-10-04 00:37] falls asleep
[1518-08-24 00:00] falls asleep
[1518-09-18 00:25] falls asleep
[1518-10-13 00:49] wakes up
[1518-11-23 00:53] wakes up
[1518-04-20 00:00] falls asleep
[1518-10-06 00:12] falls asleep
[1518-08-27 00:42] wakes up
[1518-09-24 00:54] falls asleep
[1518-06-25 00:00] Guard #2713 begins shift
[1518-07-30 00:29] falls asleep
[1518-10-13 00:10] falls asleep
[1518-09-22 00:18] wakes up
[1518-03-06 00:00] Guard #2113 begins shift
[1518-05-17 00:22] falls asleep
[1518-02-17 23:50] Guard #2297 begins shift
[1518-04-24 00:26] falls asleep
[1518-07-26 00:21] falls asleep
[1518-03-25 00:27] wakes up
[1518-06-07 00:52] falls asleep
[1518-04-16 00:23] falls asleep
[1518-11-01 00:41] wakes up
[1518-06-29 00:23] wakes up
[1518-08-01 00:40] wakes up
[1518-07-06 00:42] wakes up
[1518-08-13 00:18] falls asleep
[1518-06-13 00:50] wakes up
[1518-09-15 00:38] falls asleep
[1518-10-05 00:46] falls asleep
[1518-10-05 00:00] Guard #3407 begins shift
[1518-06-01 00:55] wakes up
[1518-09-04 00:35] falls asleep
[1518-09-24 00:59] wakes up
[1518-09-24 00:33] wakes up
[1518-02-04 00:01] falls asleep
[1518-02-23 00:07] falls asleep
[1518-05-03 00:38] falls asleep
[1518-09-17 00:49] wakes up
[1518-03-20 00:52] wakes up
[1518-08-19 00:55] falls asleep
[1518-04-06 00:56] wakes up
[1518-05-30 00:56] wakes up
[1518-09-13 23:47] Guard #1663 begins shift
[1518-04-12 00:50] wakes up
[1518-06-08 00:04] Guard #1663 begins shift
[1518-04-12 00:09] falls asleep
[1518-05-31 00:34] wakes up
[1518-02-07 00:08] falls asleep
[1518-09-02 00:22] falls asleep
[1518-09-18 00:53] wakes up
[1518-07-11 00:13] falls asleep
[1518-04-25 00:56] wakes up
[1518-04-19 23:50] Guard #739 begins shift
[1518-03-12 00:18] falls asleep
[1518-08-07 00:45] wakes up
[1518-03-14 00:29] falls asleep
[1518-10-30 00:45] falls asleep
[1518-06-20 00:00] Guard #997 begins shift
[1518-06-21 00:07] falls asleep
[1518-03-05 00:13] falls asleep
[1518-04-15 23:58] Guard #2383 begins shift
[1518-10-27 00:25] falls asleep
[1518-06-23 00:51] wakes up
[1518-07-21 00:00] Guard #2221 begins shift
[1518-10-01 00:33] wakes up
[1518-04-24 00:44] wakes up
[1518-03-21 00:27] falls asleep
[1518-02-22 00:08] falls asleep
[1518-10-05 00:47] wakes up
[1518-03-23 00:40] wakes up
[1518-11-19 00:15] falls asleep
[1518-02-21 23:57] Guard #2297 begins shift
[1518-09-08 00:40] wakes up
[1518-11-05 00:47] falls asleep
[1518-02-24 00:59] wakes up
[1518-11-06 00:12] falls asleep
[1518-10-28 00:23] falls asleep
[1518-09-20 00:04] Guard #2713 begins shift
[1518-11-15 00:33] falls asleep
[1518-09-05 00:50] wakes up
[1518-06-17 00:59] wakes up
[1518-06-08 00:08] wakes up
[1518-02-18 00:04] falls asleep
[1518-08-13 00:41] wakes up
[1518-09-19 00:21] falls asleep
[1518-11-13 00:58] wakes up
[1518-08-31 00:36] falls asleep
[1518-07-02 00:53] falls asleep
[1518-08-23 00:41] wakes up
[1518-09-14 23:57] Guard #2713 begins shift
[1518-07-24 00:44] falls asleep
[1518-02-13 00:51] wakes up
[1518-06-18 23:59] Guard #61 begins shift
[1518-08-02 00:03] Guard #1367 begins shift
[1518-06-15 00:38] falls asleep
[1518-10-01 00:02] falls asleep
[1518-10-16 00:39] wakes up
[1518-10-11 00:50] wakes up
[1518-07-02 00:25] falls asleep
[1518-02-17 00:39] wakes up
[1518-07-11 00:20] wakes up
[1518-06-23 00:04] Guard #1367 begins shift
[1518-11-11 00:27] falls asleep
[1518-10-18 00:47] wakes up
[1518-03-10 00:01] Guard #997 begins shift
[1518-05-30 23:56] Guard #997 begins shift
[1518-07-09 00:24] falls asleep
[1518-02-28 23:57] Guard #859 begins shift
[1518-05-08 00:29] wakes up
[1518-03-08 00:41] wakes up
[1518-08-25 00:57] wakes up
[1518-11-03 00:56] falls asleep
[1518-05-06 00:01] Guard #733 begins shift
[1518-11-19 23:50] Guard #3203 begins shift
[1518-06-03 00:56] wakes up
[1518-10-18 00:57] falls asleep
[1518-11-15 00:16] wakes up
[1518-07-12 00:54] falls asleep
[1518-09-19 00:57] falls asleep
[1518-03-31 00:58] wakes up
[1518-05-18 00:57] wakes up
[1518-10-19 00:03] Guard #419 begins shift
[1518-04-25 00:42] falls asleep
[1518-11-10 00:59] wakes up
[1518-05-14 00:57] wakes up
[1518-07-31 00:16] falls asleep
[1518-11-04 00:02] Guard #1367 begins shift
[1518-07-02 23:59] Guard #661 begins shift
[1518-06-27 00:04] Guard #661 begins shift
[1518-02-06 00:18] falls asleep
[1518-04-29 00:37] falls asleep
[1518-05-29 00:40] wakes up
[1518-08-25 00:35] falls asleep
[1518-06-12 00:29] wakes up
[1518-08-09 00:02] Guard #2297 begins shift
[1518-06-25 00:53] falls asleep
[1518-03-29 00:42] wakes up
[1518-03-22 00:37] falls asleep
[1518-02-14 00:34] wakes up
[1518-02-12 00:52] falls asleep
[1518-08-06 00:53] wakes up
[1518-09-02 00:00] Guard #3407 begins shift
[1518-10-02 00:34] wakes up
[1518-10-27 00:01] Guard #1663 begins shift
[1518-09-16 00:01] Guard #859 begins shift
[1518-10-07 00:34] wakes up
[1518-03-05 00:48] wakes up
[1518-03-24 00:21] falls asleep
[1518-03-10 00:24] falls asleep
[1518-10-03 00:23] falls asleep
[1518-11-03 00:49] wakes up
[1518-07-15 00:53] falls asleep
[1518-10-09 00:35] wakes up
[1518-10-12 00:49] wakes up
[1518-03-01 23:57] Guard #3407 begins shift
[1518-06-05 00:46] falls asleep
[1518-05-02 23:50] Guard #1163 begins shift
[1518-04-06 00:01] falls asleep
[1518-07-05 00:40] wakes up
[1518-05-26 23:53] Guard #113 begins shift
[1518-10-24 00:51] wakes up
[1518-10-29 00:09] falls asleep
[1518-03-07 23:57] Guard #859 begins shift
[1518-06-07 00:45] wakes up
[1518-03-01 00:16] wakes up
[1518-06-16 00:41] falls asleep
[1518-04-19 00:37] falls asleep
[1518-04-25 00:11] falls asleep
[1518-03-19 23:57] Guard #733 begins shift
[1518-11-15 00:13] falls asleep
[1518-10-13 00:00] Guard #733 begins shift
[1518-02-20 00:03] falls asleep
[1518-09-08 00:26] falls asleep
[1518-07-25 00:58] wakes up
[1518-04-23 00:03] Guard #349 begins shift
[1518-06-11 00:51] falls asleep
[1518-05-11 00:53] falls asleep
[1518-02-20 00:11] wakes up
[1518-10-29 00:16] wakes up
[1518-08-17 00:55] wakes up
[1518-08-01 00:35] falls asleep
[1518-05-26 00:52] wakes up
[1518-11-18 00:55] wakes up
[1518-02-12 00:57] falls asleep
[1518-02-15 00:12] falls asleep
[1518-09-06 00:44] wakes up
[1518-03-18 23:48] Guard #1163 begins shift
[1518-10-01 00:42] falls asleep
[1518-02-26 00:12] falls asleep
[1518-03-28 00:56] wakes up
[1518-06-06 00:46] wakes up
[1518-10-10 00:54] wakes up
[1518-11-18 23:59] Guard #419 begins shift
[1518-10-21 00:07] falls asleep
[1518-03-13 00:34] wakes up
[1518-04-21 00:55] wakes up
[1518-09-07 00:23] wakes up
[1518-03-09 00:46] wakes up
[1518-03-29 00:08] falls asleep
[1518-05-11 00:42] falls asleep
[1518-08-12 00:41] wakes up
[1518-08-05 00:39] wakes up
[1518-07-19 23:56] Guard #3391 begins shift
[1518-10-08 00:03] Guard #1367 begins shift
[1518-05-08 00:26] falls asleep
[1518-02-05 00:53] wakes up
[1518-03-11 00:10] falls asleep
[1518-05-06 00:59] wakes up
[1518-03-23 00:32] wakes up
[1518-08-19 00:59] wakes up
[1518-07-02 00:35] wakes up
[1518-10-04 00:33] wakes up
[1518-04-26 00:26] falls asleep
[1518-04-27 00:45] falls asleep
[1518-02-19 00:37] wakes up
[1518-07-12 23:58] Guard #2297 begins shift
[1518-09-19 00:02] Guard #1663 begins shift
[1518-08-23 00:47] falls asleep
[1518-03-03 00:28] wakes up
[1518-05-13 00:43] wakes up
[1518-09-10 00:04] Guard #859 begins shift
[1518-09-27 00:28] wakes up
[1518-04-11 23:56] Guard #739 begins shift
[1518-04-11 00:11] wakes up
[1518-05-31 00:44] wakes up
[1518-06-08 00:07] falls asleep
[1518-09-06 00:19] wakes up
[1518-08-02 23:50] Guard #419 begins shift
[1518-08-08 00:54] wakes up
[1518-08-19 00:06] falls asleep
[1518-10-14 00:57] falls asleep
[1518-08-27 00:01] falls asleep
[1518-08-24 00:07] wakes up
[1518-04-26 00:03] Guard #419 begins shift
[1518-08-27 00:58] wakes up
[1518-07-09 00:45] wakes up
[1518-07-20 00:18] falls asleep
[1518-07-06 00:38] falls asleep
[1518-04-22 00:39] wakes up
[1518-05-11 00:58] wakes up
[1518-07-04 00:50] wakes up
[1518-09-09 00:01] Guard #733 begins shift
[1518-07-09 00:54] wakes up
[1518-10-30 23:56] Guard #733 begins shift
[1518-11-13 00:00] Guard #3407 begins shift
[1518-04-27 00:00] Guard #739 begins shift
[1518-08-29 00:59] wakes up
[1518-10-14 00:43] wakes up
[1518-03-24 00:41] wakes up
[1518-06-05 00:34] falls asleep
[1518-05-07 00:37] falls asleep
[1518-04-13 23:57] Guard #733 begins shift
[1518-04-30 00:03] Guard #733 begins shift
[1518-07-05 00:01] Guard #419 begins shift
[1518-10-26 00:31] falls asleep
[1518-08-12 00:15] falls asleep
[1518-11-08 00:54] falls asleep
[1518-03-06 23:59] Guard #997 begins shift
[1518-06-18 00:55] wakes up
[1518-11-09 00:21] falls asleep
[1518-04-02 23:51] Guard #2113 begins shift
[1518-06-05 00:57] wakes up
[1518-08-11 23:57] Guard #997 begins shift
[1518-06-18 00:39] wakes up
[1518-11-12 00:46] wakes up
[1518-06-03 00:46] falls asleep
[1518-08-30 00:48] wakes up
[1518-06-02 23:58] Guard #859 begins shift
[1518-07-13 00:56] falls asleep
[1518-11-03 00:36] falls asleep
[1518-02-12 00:39] wakes up
[1518-04-09 00:14] falls asleep
[1518-07-10 00:34] wakes up
[1518-06-08 00:18] falls asleep
[1518-09-04 23:58] Guard #1663 begins shift
[1518-05-12 00:52] wakes up
[1518-09-30 23:48] Guard #3407 begins shift
[1518-08-10 00:17] wakes up
[1518-03-29 23:56] Guard #2221 begins shift
[1518-06-29 00:55] wakes up
[1518-09-10 00:57] falls asleep
[1518-04-01 00:02] falls asleep
[1518-07-13 00:34] falls asleep
[1518-06-17 00:15] falls asleep
[1518-04-19 00:00] Guard #997 begins shift
[1518-10-13 00:59] wakes up
[1518-10-26 00:46] wakes up
[1518-02-21 00:56] wakes up
[1518-04-25 00:37] wakes up
[1518-03-24 00:02] Guard #113 begins shift
[1518-04-26 00:49] wakes up
[1518-03-18 00:50] wakes up
[1518-03-08 00:37] falls asleep
[1518-08-15 00:50] falls asleep
[1518-03-25 00:42] wakes up
[1518-02-14 00:37] falls asleep
[1518-11-10 23:58] Guard #2221 begins shift
[1518-06-05 00:40] wakes up
[1518-07-21 00:34] wakes up
[1518-10-01 00:51] wakes up
[1518-09-17 00:48] falls asleep
[1518-05-17 00:54] wakes up
[1518-02-27 00:47] wakes up
[1518-09-03 00:19] falls asleep
[1518-06-16 00:58] wakes up
[1518-02-10 00:16] falls asleep
[1518-05-09 00:55] wakes up
[1518-07-31 00:01] Guard #1367 begins shift
[1518-09-05 00:11] falls asleep
[1518-02-25 00:51] wakes up
[1518-06-16 00:20] wakes up
[1518-04-20 00:01] wakes up
[1518-07-16 00:59] wakes up
[1518-10-22 00:53] wakes up
[1518-07-05 00:21] falls asleep
[1518-08-26 23:47] Guard #3407 begins shift
[1518-09-06 23:59] Guard #2297 begins shift
[1518-05-12 00:00] Guard #113 begins shift
[1518-06-13 00:35] wakes up
[1518-07-24 00:04] Guard #2221 begins shift
[1518-02-16 00:04] falls asleep
[1518-09-20 00:57] wakes up
[1518-10-25 00:38] falls asleep
[1518-03-13 00:18] falls asleep
[1518-05-25 00:26] falls asleep
[1518-02-22 00:53] wakes up
[1518-03-28 00:42] wakes up
[1518-02-23 00:04] Guard #419 begins shift
[1518-03-13 23:59] Guard #3407 begins shift
[1518-10-02 00:32] falls asleep
[1518-05-03 00:50] falls asleep
[1518-10-27 00:54] wakes up
[1518-02-08 00:00] Guard #2383 begins shift
[1518-03-25 00:35] falls asleep
[1518-08-19 00:40] wakes up
[1518-08-07 00:53] falls asleep
[1518-02-14 00:31] falls asleep
[1518-08-26 00:04] Guard #2383 begins shift
[1518-04-15 00:03] Guard #661 begins shift
[1518-02-27 00:09] falls asleep
[1518-09-04 00:41] wakes up
[1518-08-20 00:00] Guard #739 begins shift
[1518-03-31 23:53] Guard #419 begins shift
[1518-10-17 00:58] wakes up
[1518-07-24 00:56] falls asleep
[1518-10-23 00:24] wakes up
[1518-08-18 23:56] Guard #1163 begins shift
[1518-06-05 00:12] falls asleep
[1518-04-14 00:56] wakes up
[1518-07-14 00:00] Guard #2713 begins shift
[1518-04-28 00:57] wakes up
[1518-05-07 00:02] Guard #1367 begins shift
[1518-05-15 00:01] falls asleep
[1518-10-11 23:59] Guard #3391 begins shift
[1518-10-14 00:38] falls asleep
[1518-06-12 00:17] falls asleep
[1518-05-24 23:59] Guard #997 begins shift
[1518-11-17 00:53] wakes up
[1518-03-09 00:10] wakes up
[1518-03-28 00:17] falls asleep
[1518-10-30 00:50] wakes up
[1518-02-13 00:35] falls asleep
[1518-09-10 00:54] wakes up`

val input2 = `[1518-11-01 00:00] Guard #10 begins shift
[1518-11-01 00:05] falls asleep
[1518-11-01 00:25] wakes up
[1518-11-01 00:30] falls asleep
[1518-11-01 00:55] wakes up
[1518-11-01 23:58] Guard #99 begins shift
[1518-11-02 00:40] falls asleep
[1518-11-02 00:50] wakes up
[1518-11-03 00:05] Guard #10 begins shift
[1518-11-03 00:24] falls asleep
[1518-11-03 00:29] wakes up
[1518-11-04 00:02] Guard #99 begins shift
[1518-11-04 00:36] falls asleep
[1518-11-04 00:46] wakes up
[1518-11-05 00:03] Guard #99 begins shift
[1518-11-05 00:45] falls asleep
[1518-11-05 00:55] wakes up`

class EventType
class Begins: EventType =
	Var Id: Int
	Def new(id: Int) = Id = id

	Def toString() = "Begins(" + Id + ")"

class FallAsleep: EventType
class WakeUp: EventType

class Event =
	Var Date: Date
	Var EventType: EventType

	Def new(date: Date, eventType: EventType) =
		Date = date
		EventType = eventType

	Def toString() = "[" + Date.toString() + "]: " + EventType

class EventComparator: Comparator<Event> =
	Def Compare(a: Event, b: Event) = a.Date.compareTo(b.Date)


Def GetEvents(input: String) =
	val parser = new SimpleDateFormat("[yyyy-MM-dd HH:mm]");
	val r = Pattern.compile(`Guard \#(\d+) begins shift`)

	val events = new Vector<Event>()

	for(val line in input.Lines())
		val date = parser.parse(line[0:18])
		var eventType: EventType
		val t = line[19:]
		if(t == "falls asleep")  eventType = new FallAsleep()
		else if(t == "wakes up") eventType = new WakeUp()
		else
			val m = r.matcher(t)
			m.matches()
			eventType = new Begins(m.group(1).ToInt())

		events.Add(new Event(date, eventType))

	events.Sort(new EventComparator())
	events

Def GetSleepyTimes(events: Vector<Event>) =
	val times = new HashMap<Int, Long>()
	var id = -1
	var start = new Date(0)
	for(val event in events)
		if(event.EventType is Begins)
			val begins = event.EventType as Begins
			id = begins.Id
		else if(event.EventType is FallAsleep)
			start = event.Date
		else if(event.EventType is WakeUp)
			val durationMs = event.Date.getTime() - start.getTime()
			times[id] = (times.Get(id) ?: 0) + durationMs / 60000
	times

Def GetSleepiestGuardId(sleepyTimes: HashMap<Int, Long>) =
	var id = -1
	var time = -1L
	for(val e in sleepyTimes)
		if(e.Value() >= time)
			time = e.Value()
			id = e.Key()
	id

Def GetGuardsEvents(allEvents: Vector<Event>, id: Int) =
	val events = new Vector<Event>()
	var currentId = 0
	for(val event in allEvents)
		if(event.EventType is Begins)
			val begins = event.EventType as Begins
			currentId = begins.Id
		else if(currentId == id)
			events.Add(event)
	events

Def GetSleepyMinutes(allEvents: Vector<Event>, id: Int) =
	val events = GetGuardsEvents(allEvents, id)
	val minutes = new HashMap<Int, Int>()

	var start = 0
	for(val event in events)
		if(event.EventType is FallAsleep)
			start = event.Date.getMinutes()
		else if(event.EventType is WakeUp)
			val end = event.Date.getMinutes()
			for(var i = start; i < end; i++)
				minutes[i] = (minutes.Get(i) ?: 0) + 1

	minutes

Def GetSleepiestMinute(minutes: HashMap<Int, Int>) =
	var max = 0
	var minute = 0
	for(val m in minutes)
		if(m.Value() >= max)
			max = m.Value()
			minute = m.Key()
	minute

val events = GetEvents(input)
val sleepyTimes = GetSleepyTimes(events)
val id = GetSleepiestGuardId(sleepyTimes)
val minutes = GetSleepyMinutes(events, id)
val minute = GetSleepiestMinute(minutes)

println(id * minute) // res: 35184