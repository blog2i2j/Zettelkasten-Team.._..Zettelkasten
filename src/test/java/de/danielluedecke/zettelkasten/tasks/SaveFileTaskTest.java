package de.danielluedecke.zettelkasten.tasks;

import ch.unibe.jexample.Given;
import ch.unibe.jexample.JExample;
import de.danielluedecke.zettelkasten.database.*;
import de.danielluedecke.zettelkasten.settings.Settings;
import org.jdesktop.application.Application;
import org.jdesktop.application.ApplicationContext;
import org.jdesktop.application.ResourceMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.swing.*;
import java.io.File;

import static org.junit.Assert.*;

@RunWith(JExample.class)
public class SaveFileTaskTest {

    private SaveFileTask task;
    private Application mockApp;
    private Daten mockData;
    private Settings mockSettings;
    private JLabel mockLabel;
    private JDialog mockDialog;

    // Test 1: Initialize resources for further tests
    @Test
    public void initializeResources() {
        // Mock the Application and its ApplicationContext
        mockApp = Mockito.mock(Application.class);
        ApplicationContext mockContext = Mockito.mock(ApplicationContext.class);
        ResourceMap mockResourceMap = Mockito.mock(ResourceMap.class);

        // Configure the mock ResourceMap to return a mock string for any key
        Mockito.when(mockResourceMap.getString(Mockito.anyString())).thenReturn("mockMessage");

        // Configure the ApplicationContext to return the mock ResourceMap
        Mockito.when(mockContext.getResourceMap()).thenReturn(mockResourceMap);
        Mockito.when(mockApp.getContext()).thenReturn(mockContext);

        // Mock other dependencies
        mockData = Mockito.mock(Daten.class);
        mockSettings = Mockito.mock(Settings.class);
        mockLabel = Mockito.mock(JLabel.class);
        mockDialog = Mockito.mock(JDialog.class);

        File tempFile = new File("mockSave.zip");
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(tempFile);
        Mockito.doNothing().when(mockLabel).setText(Mockito.anyString());

        // Initialize the task with mocked dependencies
        task = new SaveFileTask(mockApp, mockDialog, mockLabel, mockData,
                Mockito.mock(Bookmarks.class), Mockito.mock(SearchRequests.class),
                Mockito.mock(DesktopData.class), Mockito.mock(Synonyms.class),
                mockSettings, Mockito.mock(BibTeX.class));

        // Assertions and verifications
        assertNotNull(task);
        Mockito.verify(mockSettings).getMainDataFile();
        Mockito.verify(mockLabel).setText(Mockito.anyString());
    }

    // Test 2: Verify save process in doInBackground
    @Test
    @Given("initializeResources")
    public void shouldSaveDataCorrectly() throws Exception {
        // Setup: configure mocks to simulate successful save path
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(new File("mockSave.zip"));
        // Run
        task.doInBackground();
        // Assert that the saveOk flag remains true after successful save
        assertTrue("Save should be successful", task.saveOk);
    }

    // Test 3: Simulate error in saving process
    @Test
    @Given("initializeResources")
    public void shouldHandleSaveErrorGracefully() {
        // Setup: Simulate a file path error by returning null from settings
        Mockito.when(mockSettings.getMainDataFile()).thenReturn(null);
        // Run
        task.doInBackground();
        // Verify error was handled and saveOk was set to false
        assertFalse("Save should fail due to null file path", task.saveOk);
    }

    // Test 4: Verify modified flags are set correctly after save
    @Test
    @Given("shouldSaveDataCorrectly")
    public void shouldSetModifiedFlagsOnSuccess() {
        // Run
        task.succeeded(null);
        // Verify all flags were set based on saveOk
        Mockito.verify(mockData).setModified(false);
        if (task.saveOk) {
            Mockito.verify(mockData).setModified(false);
        }
    }

    // Test 5: Verify dialog is disposed in the finished method
    @Test
    @Given("shouldSaveDataCorrectly")
    public void shouldDisposeDialogOnFinished() {
        // Run
        task.finished();
        // Verify dialog is disposed
        Mockito.verify(mockDialog).dispose();
    }
}
