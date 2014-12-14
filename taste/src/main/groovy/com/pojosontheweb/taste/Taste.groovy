package com.pojosontheweb.taste

import com.pojosontheweb.selenium.DriverBuildr
import com.pojosontheweb.selenium.Findr
import groovy.json.JsonBuilder
import org.openqa.selenium.WebDriver
import org.pojosontheweb.selenium.groovy.DollrCategory
import org.pojosontheweb.selenium.groovy.FindrCategory
import org.pojosontheweb.selenium.groovy.ListFindrCategory
import org.pojosontheweb.selenium.groovy.WebDriverCategory

class Taste {

    private static void invalidArgs() {

    }

    static void main(String[] args) {

        def cli = new CliBuilder(usage:'taste [options] files...', posix: false)
        cli.b(longOpt:'browser', args:1, argName:'browser', 'browser to use (chrome or firefox, defaults to FF)')
        cli.v(longOpt:'verbose', 'show logs')
        cli.j(longOpt:'json', 'output json')

        def invalidArgs = {
            cli.usage()
            System.exit(0)
        }

        def options = cli.parse(args)

        if (!options) {
            invalidArgs()
        }

        def files = options.arguments()
        if (!files) {
            invalidArgs()
        }

        boolean verbose = false
        if (options.v) {
            verbose = true
            System.setProperty(Findr.SYSPROP_VERBOSE, "true")
        }

        if (options.b) {
            System.setProperty(DriverBuildr.SysPropsBuildr.PROP_WEBTESTS_BROWSER, options.b)
        }

        def log = { msg ->
            if (verbose) {
                println msg
            }
        }

        log("""_/_/_/_/_/                      _/
   _/      _/_/_/    _/_/_/  _/_/_/_/    _/_/
  _/    _/    _/  _/_/        _/      _/_/_/_/
 _/    _/    _/      _/_/    _/      _/
_/      _/_/_/  _/_/_/        _/_/    _/_/_/
""")

        String fileName = files[0]

        log("Running $fileName (${options.b})...")

        Binding b = new Binding()
        GroovyShell shell = new CustomShell(b)
        // TODO handle cast in case folks try to do something else than running tests
        def res = shell.evaluate(new InputStreamReader(new FileInputStream(fileName)))

        log("...$fileName evaluated, will now run tests")

        if (res instanceof Test) {
            Test test = (Test)res
            def testResult = test.execute()

            def printJson = {
                Map m = testResult.toMap()
                m['fileName'] = fileName
                println new JsonBuilder(m).toPrettyString()
            }

            if (testResult instanceof ResultFailure) {
                ResultFailure f = (ResultFailure)testResult
                if (options.j) {
                    printJson()
                } else {
                    println("""Test '$test.name' FAILED : $f.err.message
- fileName      : $fileName
- startedOn     : $f.startedOn
- finishedOn    : $f.finishedOn
- stackTrace    : $f.stackTrace""")
                }
            } else if (testResult instanceof ResultSuccess) {
                ResultSuccess s = (ResultSuccess)testResult
                if (options.j) {
                    printJson()
                } else {
                    println("""Test '$test.name' SUCCESS
- fileName      : $fileName
- startedOn     : $s.startedOn
- finishedOn    : $s.finishedOn
- retVal        : $s.retVal""")
                }

            } else {
                println("""Test '$test.name' FAILED with invalid return value $testResult ($fileName)""")
            }

        } else {
            throw new IllegalStateException("File $fileName returned invalid Test : $res")
        }

        System.exit(0)
    }

    static Test test(String testName, @DelegatesTo(TestContext) Closure c) {
        new Test(name: testName, body: c)
    }

}

class CustomShell extends GroovyShell {


    CustomShell(Binding binding) {
        super(binding)
    }

    @Override
    protected synchronized String generateScriptName() {
        return "Skunk"

    }
}
