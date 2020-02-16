package ch.ninecode.mscons

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.util.parsing.input.Reader

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import ch.ninecode.edifact.Segment

//    Pos     Tag Name                                     S   R
//
//    HEADER SECTION
//
//    00010   UNH Message header                           M   1
//    00020   BGM Beginning of message                     M   1
//    00030   DTM Date/time/period                         M   9
//    00040   CUX Currencies                               C   9
//
//    00050       ---- Segment group 1  ------------------ C   9----------------+
//    00060   RFF Reference                                M   1                |
//    00070   DTM Date/time/period                         C   9----------------+
//
//    00080       ---- Segment group 2  ------------------ C   99---------------+
//    00090   NAD Name and address                         M   1                |
//    |
//    00100       ---- Segment group 3  ------------------ C   9---------------+|
//    00110   RFF Reference                                M   1               ||
//    00120   DTM Date/time/period                         C   9---------------+|
//    |
//    00130       ---- Segment group 4  ------------------ C   9---------------+|
//    00140   CTA Contact information                      M   1               ||
//    00150   COM Communication contact                    C   9---------------++
//
//    DETAIL SECTION
//
//    00160   UNS Section control                          M   1
//
//    00170       ---- Segment group 5  ------------------ M   99999------------+
//    00180   NAD Name and address                         M   1                |
//    |
//    00190       ---- Segment group 6  ------------------ M   99999-----------+|
//    00200   LOC Place/location identification            M   1               ||
//    00210   DTM Date/time/period                         C   9               ||
//    ||
//    00220       ---- Segment group 7  ------------------ C   99-------------+||
//    00230   RFF Reference                                M   1              |||
//    00240   DTM Date/time/period                         C   9--------------+||
//    ||
//    00250       ---- Segment group 8  ------------------ C   99-------------+||
//    00260   CCI Characteristic/class id                  M   1              |||
//    00270   DTM Date/time/period                         C   99-------------+||
//    ||
//    00280       ---- Segment group 9  ------------------ C   99999----------+||
//    00290   LIN Line item                                M   1              |||
//    00300   PIA Additional product id                    C   9              |||
//    00310   IMD Item description                         C   9              |||
//    00320   PRI Price details                            C   9              |||
//    00330   NAD Name and address                         C   9              |||
//    00340   MOA Monetary amount                          C   9              |||
//    |||
//    00350       ---- Segment group 10 ------------------ M   9999----------+|||
//    00360   QTY Quantity                                 M   1             ||||
//    00370   DTM Date/time/period                         C   9             ||||
//    00380   STS Status                                   C   9-------------+|||
//    |||
//    00390       ---- Segment group 11 ------------------ C   99------------+|||
//    00400   CCI Characteristic/class id                  M   1             ||||
//    00410   MEA Measurements                             C   99            ||||
//    00420   DTM Date/time/period                         C   9-------------++++
//
//    SUMMARY SECTION
//
//    00430   CNT Control total                            C   99
//    00440   UNT Message trailer                          M   1

case class Group1 (rff: RFF, dtm_p: Option[DTM])

case class MSCONSMessage04B (
    bgm: BGM,
    dtm: DTM,
    cux: Option[CUX],
    group1: Option[List[Group1]]
)

object MSCONSMessage04B extends MSCONSMessage
{
    lazy val bgm: Parser[BGM] = expect ("BGM", x => BGM (x))
    lazy val dtm: Parser[DTM] = expect ("DTM", x => DTM (x))
    lazy val cux: Parser[CUX] = expect ("CUX", x => CUX (x))
    lazy val rff: Parser[RFF] = expect ("RFF", x => RFF (x))
    lazy val group1: Parser[Option[List[Group1]]] = repAtMostN (9, false, rff ~ dtm.?).? ^^
        (g => if (g.isDefined) Some (g.get.map (x => Group1 (x._1, x._2))) else None)

    val phrase: Parser[MSCONSMessage04B] = bgm ~ dtm ~ cux.? ~ group1 ^^
        { case bgm ~ dtm ~ cux ~ group1 => MSCONSMessage04B (bgm, dtm, cux, group1) }
}
