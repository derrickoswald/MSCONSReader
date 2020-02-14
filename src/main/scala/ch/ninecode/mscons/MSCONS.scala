package ch.ninecode.mscons

import java.util.Properties

import org.slf4j.LoggerFactory
import org.slf4j.Logger

object MSCONS
{
    def main (args: Array[String]): Unit =
    {
        val log: Logger = LoggerFactory.getLogger (getClass)
        val properties =
        {
            val in = this.getClass.getResourceAsStream ("/app.properties")
            val p = new Properties ()
            p.load (in)
            in.close ()
            p
        }
        val APPLICATION_NAME: String = properties.getProperty ("artifactId")
        val APPLICATION_VERSION: String = properties.getProperty ("version")
        //val SPARK: String = properties.getProperty ("spark")

        new MSCONSOptionsParser (APPLICATION_NAME, APPLICATION_VERSION).parse (args, MSCONSOptions ()) match
        {
            case Some (options) =>
                if (options.valid)
                {
                    //                    if (options.verbose) org.apache.log4j.LogManager.getLogger (getClass.getName).setLevel (org.apache.log4j.Level.INFO)

                    if (options.mscons.nonEmpty)
                        for (name <- options.mscons)
                            MSCONSParser (options).parse (name)
                    else
                        log.error ("no input MSCONS files specified")
                }
                if (!options.unittest)
                    sys.exit (0)
            case None =>
                sys.exit (1)
        }
    }
}