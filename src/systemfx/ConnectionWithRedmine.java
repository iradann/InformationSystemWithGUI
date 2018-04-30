/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package systemfx;

import com.taskadapter.redmineapi.AttachmentManager;
import com.taskadapter.redmineapi.Include;
import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.ProjectManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.UserManager;
import com.taskadapter.redmineapi.bean.Attachment;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.Version;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.codec.Charsets;
import org.apache.http.entity.ContentType;

public class ConnectionWithRedmine {

    private String url;
    private String apiAccessKey;
    private String projectKey;
    private Integer queryId = null;
    int javaErrorAmount = 0;
    double pythonRating = 0;
    String perevod = null;

    private RedmineManager mgr;
    private IssueManager issueManager;
    private AttachmentManager attachmentManager;
    private List<Issue> issues;
    private ProjectManager projectManager;
    private List<Project> projects;
    private UserManager userManager;
    private List<User> users;
    private List<Version> versions;

    public ConnectionWithRedmine(String key, String key2, String url) {
        this.apiAccessKey = key;
        this.projectKey = key2;
        this.url = url;
        this.mgr = RedmineManagerFactory.createWithApiKey(url, apiAccessKey);
        this.issueManager = mgr.getIssueManager();
        this.attachmentManager = mgr.getAttachmentManager();
        this.projectManager = mgr.getProjectManager();
        this.userManager = mgr.getUserManager();
    }

    public void saveAttachment(Issue issue) throws IOException {
        Collection<Attachment> issueAttachment = issue.getAttachments();
        ArrayList<Attachment> issueAttachments = new ArrayList<>(issueAttachment);
        File dir = new File(".\\myFiles\\");
        dir.mkdirs();

        for (Attachment attach : issueAttachments) {

            if (attach.getFileName().endsWith(".py") || attach.getFileName().endsWith(".java") || attach.getFileName().endsWith(".cpp")) {

                if (checkAttachmentID(attach.getId()) == 0) {

                    String fileToManage = ".\\myFiles\\" + attach.getFileName();
                    downloadAttachments(attach.getContentURL(),
                            apiAccessKey,
                            fileToManage);

                }
            } else {
                continue;
            }

        }
    }

    private void downloadAttachments(String url, String apikey, String fileName) throws MalformedURLException, IOException {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("x-redmine-api-key", apikey)
                .addHeader("cache-control", "no-cache") //не обязательно
                .build();

        Response response = client.newCall(request).execute();

        try (InputStream in = response.body().byteStream()) {
            Path to = Paths.get(fileName); //convert from String to Path
            Files.copy(in, to, StandardCopyOption.REPLACE_EXISTING);
        }

    }

    public List<Issue> getIssues() throws RedmineException {
        issues = issueManager.getIssues(projectKey, queryId, Include.journals, Include.attachments, Include.changesets);
        return issues;
    }

   
    public Collection<Version> getVersions(String projectKey) throws RedmineException {
        Project project = projectManager.getProjectByKey(projectKey);
        int projectID = project.getId();
        versions = projectManager.getVersions(projectID);
        return versions;
    }

    public Issue getIssueByID(int issueID) throws RedmineException {
        Issue issue = issueManager.getIssueById(issueID, Include.journals);
        return issue;
    }

    public User getUser() throws RedmineException {
        User user = userManager.getCurrentUser();
        return user;
    }

    public void uploadAttachment(Issue issue, String path) {

        try {
            String filename = path;
            File file = new File(filename);
            attachmentManager.addAttachmentToIssue(issue.getId(), file, ContentType.TEXT_PLAIN.getMimeType());
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    //срабатывает, но неверно загружает поток данных на сайт -- не как .zip файл
    public void uploadAttachmentSonar(Issue issue, String path) throws RedmineException, IOException {

        String filename = path;
        File file = new File(filename);
        // не работает пока что...
        attachmentManager.addAttachmentToIssue(issue.getId(), file, ContentType.create("application/zip").getMimeType());

    }

    public int checkAttachmentID(Integer id) throws IOException {
        List<String> attachmentIDs = new ArrayList<String>();
        attachmentIDs = Files.readAllLines(Paths.get("AttachmentID.txt"), Charsets.UTF_8);
        int response = 0;
        int attachWasCheckedBefore = 1;
        int attachIsNew = 0;

        for (String attach : attachmentIDs) {
            int idFromFile = Integer.parseInt(attach);
            if (idFromFile == id) {
                response = attachWasCheckedBefore;
                break;
            } else {
                response = attachIsNew;
            }
        }
        if (response == attachIsNew) {
            String fromIntToString = Integer.toString(id) + "\r\n";
            Files.write(Paths.get("AttachmentID.txt"), fromIntToString.getBytes(), StandardOpenOption.APPEND);

        }
        return response;
    }

    private void removeDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
            dir.delete();
        } else {
            dir.delete();
        }
    }

    private void cleanDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 0) {
                for (File aFile : files) {
                    removeDirectory(aFile);
                }
            }
        }
    }

    private String readLastLineInFile(String fileDir) {
        List<String> lines = new ArrayList<String>();
        try {
            lines = Files.readAllLines(Paths.get(fileDir), Charsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
        String result = null;
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (!lines.get(i).isEmpty()) {
                result = lines.get(i);
                break;
            } else {
                continue;
            }
        }

        return result;
    }

    public Integer javaErrorAmount(String string) {

        ArrayList<String> words = new ArrayList<>();
        if (!string.isEmpty()) {
            for (String retval : string.split(" ")) {
                words.add(retval);
            }
        }

        String neededNumber = words.get((words.size()) - 2);
        int errorAmount = Integer.parseInt(neededNumber);

        return errorAmount;
    }

    public Double pythonRatingCheck(String string) {
        if (!string.isEmpty()) {

        }
        return 1.1;
    }

    public void updateIssue(Issue issue) {
        try {
            issueManager.update(issue);
        } catch (RedmineException ex) {
            Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setVersionForCheck(String inputTargetVersion, Issue issue) {

        Version version = issue.getTargetVersion();
        Collection<Attachment> attach = issue.getAttachments();

        if (inputTargetVersion.equals("All")) {
            try {
                this.saveAttachment(issue);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (version == null) {
            System.out.println("Issue without target version");
        } else if (version.getName().equals(inputTargetVersion)) {
            try {
                this.saveAttachment(issue);
            } catch (IOException ex) {
                Logger.getLogger(ConnectionWithRedmine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
