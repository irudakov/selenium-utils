package com.pojosontheweb.tastecloud.actions

import com.pojosontheweb.selenium.Browsr
import com.pojosontheweb.tastecloud.model.Config
import com.pojosontheweb.tastecloud.model.Run
import com.pojosontheweb.tastecloud.woko.DockerManager
import com.pojosontheweb.tastecloud.woko.TasteStore
import com.spotify.docker.client.LogMessage
import net.sourceforge.stripes.action.DefaultHandler
import net.sourceforge.stripes.action.DontValidate
import net.sourceforge.stripes.action.ForwardResolution
import net.sourceforge.stripes.action.Resolution
import net.sourceforge.stripes.action.UrlBinding
import net.sourceforge.stripes.validation.Validate
import org.apache.commons.io.FileUtils
import woko.Woko
import woko.actions.BaseActionBean
import woko.async.JobBase
import woko.async.JobListener
import woko.async.JobManager
import woko.persistence.TransactionCallbackWithResult
import woko.util.WLogger

import java.nio.ByteBuffer

@UrlBinding('/run')
class RunAction extends BaseActionBean {

    @Validate(required = true)
    Browsr browsr

    @Validate(required = true)
    String taste

    @DefaultHandler
    @DontValidate
    Resolution display() {
        new ForwardResolution('/WEB-INF/jsp/run.jsp')
    }

    Resolution run() {
        // store the run
        TasteStore store = (TasteStore)woko.objectStore
        Run run = new Run(
            id: UUID.randomUUID().toString(),
            browsr: browsr,
            taste: taste
        )
        store.save(run)

        // start the job in bg...
        Config config = store.config
        JobManager jobManager = woko.ioc.getComponent(JobManager.KEY)
        jobManager.submit(new RunJob(woko, run.id, new File(config.webappDir), config.dockerUrl, new File(config.dockerDir)), [])

        // redirect to run view
        woko.resolutions().redirect('view', run)
    }

}

class RunJob extends JobBase {

    private static final WLogger logger = WLogger.getLogger(RunJob.class)

    private final Woko woko
    private final String runId
    private final File webappDir
    private final File dockerDir
    private final String dockerUrl

    RunJob(Woko woko, String runId, File webappDir, String dockerUrl, File dockerDir) {
        this.woko = woko
        this.runId = runId
        this.webappDir = webappDir
        this.dockerUrl = dockerUrl
        this.dockerDir = dockerDir
    }

    private def withRun(Closure c) {
        TasteStore store = (TasteStore)woko.objectStore
        store.inTx {
            Run run = store.getRun(runId)
            c(store, run)
        }
    }

    private static def log(Run run, String msg) {
        run.logs = (run.logs ?: '') + msg + '\n'
    }

//    private def withTmpDir(Closure c) {
//        File tmpDir = new File(baseDir, UUID.randomUUID().toString())
//        tmpDir.mkdirs()
//        try {
//            return c(tmpDir)
//        } finally {
//            FileUtils.deleteDirectory(tmpDir)
//        }
//    }

    private static String logToString(LogMessage lm) {
        ByteBuffer bb = lm.content()
        byte[] b = new byte[bb.remaining()]
        bb.get(b)
        new String(b, 'utf-8')
    }

    @Override
    protected void doExecute(List<JobListener> listeners) {

        logger.info("$runId : starting, webappDir=$webappDir, dockerDir=$dockerDir")

        try {
            String tasteTxt = withRun { TasteStore store, Run run ->
                log run, 'Run started...'
                run.startedOn = new Date()
                store.save run
                store.session.flush()
                run.taste
            }

            // create a folder in tmp dir to store
            // the taste run data
            File webappFullDir = new File(webappDir, runId)
            webappFullDir.mkdirs()
            try {

                // store taste script to file
                File tasteFile = new File(webappFullDir, 'tests.taste')
                tasteFile.text = tasteTxt

                logger.info("$runId : tests file written in $tasteFile.absolutePath")

                // create a config file
                File cfgFile = new File(webappFullDir, 'cfg.taste')
                cfgFile.text = '''import static com.pojosontheweb.taste.Cfg.*

config {

    output {
        json()
    }

    firefox()

//    sysProps['webdriver.chrome.driver'] = "${System.getProperty('user.home')}/chromedriver"

//    locales "en", "fr"                      // locale(s) to be used

    findr {
        timeout 10
        verbose true
    }

    video {
        enabled true
        dir "/mnt/target"
        failuresOnly false
    }
}'''

                // run the taste file into a docker of its own !
                DockerManager dm = woko.ioc.getComponent(DockerManager.KEY)

                // store logs
                // TODO buffer : for now it's heavy db stress for nothing !
                File dockerFullDir = new File(dockerDir, runId) // new File('/media/psf/projects/selenium-utils/taste/docker/sample')
                dm.startRun(dockerUrl, dockerFullDir) { LogMessage lm ->
                    withRun { TasteStore s, Run run ->
                        String msg = logToString lm
                        run.logs = run.logs + msg
                        s.save run
                    }
                }

                // retrieve the results from target dir and
                // copy to webapp disk
                File resultsDir = resultsDir(webappDir, runId)
                if (!resultsDir.exists()) {
                    resultsDir.mkdirs()
                }
                File webappTargetDir = new File(webappFullDir, 'target')
                FileUtils.copyDirectory(webappTargetDir, resultsDir)

            } finally {
                FileUtils.deleteDirectory(webappFullDir)
            }

        } finally {
            withRun { TasteStore store, Run run ->
                log run, 'Run finished'
                run.finishedOn = new Date()
                store.save(run)
            }
            logger.info("$runId : done")
        }

    }

    static File resultsDir(File webappDir, String runId) {
        new File(webappDir.absolutePath + File.separator + 'results' + File.separator + runId)
    }
}