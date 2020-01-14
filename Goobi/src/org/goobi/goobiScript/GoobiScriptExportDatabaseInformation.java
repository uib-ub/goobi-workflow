package org.goobi.goobiScript;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.export.ExportXmlLog;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import com.google.common.collect.ImmutableList;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptExportDatabaseInformation extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        ImmutableList.Builder<GoobiScriptResult> newList = ImmutableList.<GoobiScriptResult> builder().addAll(gsm.getGoobiScriptResults());
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            newList.add(gsr);
        }
        gsm.setGoobiScriptResults(newList.build());

        return true;
    }

    @Override
    public void execute() {
        ExportThread et = new ExportThread();
        et.start();
    }

    class ExportThread extends Thread {

        @Override
        public void run() {
            // wait until there is no earlier script to be executed first
            while (gsm.getAreEarlierScriptsWaiting(starttime)) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Problem while waiting for running GoobiScripts", e);
                }
            }

            // execute all jobs that are still in waiting state
            for (GoobiScriptResult gsr : gsm.getGoobiScriptResults()) {
                if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                    Process p = ProcessManager.getProcessById(gsr.getProcessId());
                    try {
                        gsr.setProcessTitle(p.getTitel());
                        gsr.setResultType(GoobiScriptResultType.RUNNING);
                        gsr.updateTimestamp();

                        Path dest = null;
                        try {
                            dest = Paths.get(p.getProcessDataDirectoryIgnoreSwapping(), p.getId() + "_db_export.xml");
                        } catch (IOException | InterruptedException | SwapException | DAOException e) {
                            log.error(e);
                            gsr.setResultMessage("Cannot read process folder " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                        OutputStream os = null;

                        try {
                            os = Files.newOutputStream(dest);
                        } catch (IOException e) {
                            log.error(e);
                            gsr.setResultMessage("Cannot write into export file " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                        try {
                            Document doc = new ExportXmlLog().createExtendedDocument(p);
                            XMLOutputter outp = new XMLOutputter();
                            outp.setFormat(Format.getPrettyFormat());
                            outp.output(doc, os);
                        } catch (IOException e) {
                            log.error(e);
                            gsr.setResultMessage("Cannot write into export file " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        } finally {
                            if (os != null) {
                                os.close();
                            }
                        }
                        if (gsr.getResultType() != GoobiScriptResultType.ERROR) {
                            gsr.setResultMessage("Export done successfully");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        }

                    } catch (NoSuchMethodError | Exception e) {
                        gsr.setResultMessage(e.getMessage());
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        gsr.setErrorText(e.getMessage());
                        log.error("Exception during the export of database information for id " + p.getId(), e);
                    }
                    gsr.updateTimestamp();
                }
            }
        }
    }

}
