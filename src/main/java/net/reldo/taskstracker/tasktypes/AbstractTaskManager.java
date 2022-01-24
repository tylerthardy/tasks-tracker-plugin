package net.reldo.taskstracker.tasktypes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import javax.swing.SwingUtilities;
import net.reldo.taskstracker.TasksTrackerPlugin;
import net.reldo.taskstracker.data.TaskDataClient;
import net.reldo.taskstracker.data.TaskSave;
import net.reldo.taskstracker.data.TrackerDataStore;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;

public abstract class AbstractTaskManager
{
	protected final TrackerDataStore trackerDataStore;
	private TaskDataClient taskDataClient;
	private final TasksTrackerPlugin plugin;
	public TaskType taskType;
	public ArrayList<Task> tasks;
	public int maxTaskCount;

	public AbstractTaskManager(TaskType taskType, TasksTrackerPlugin plugin, TrackerDataStore trackerDataStore, TaskDataClient taskDataClient)
	{
		this.taskType = taskType;
		this.plugin = plugin;
		this.trackerDataStore = trackerDataStore;
		this.taskDataClient = taskDataClient;
	}

	public void loadTaskSourceData()
	{
		taskDataClient.loadTaskSourceData(taskType, (tasks) -> {
			this.tasks = tasks;
			applyTrackerSave();
		});
	}

	public void applyTrackerSave()
	{
		HashMap<String, TaskSave> loadedTasks = trackerDataStore.currentData.tasksByType.get(taskType);
		if (loadedTasks == null)
		{
			return;
		}
		tasks.forEach(task -> {
			TaskSave taskSave = loadedTasks.get(task.getName());
			if (taskSave == null)
			{
				task.setTrackedOn(0);
				task.setCompletedOn(0);
				task.setIgnoredOn(0);
				return;
			}
			task.setTrackedOn(taskSave.getTrackedOn());
			task.setCompletedOn(taskSave.getCompletedOn());
			task.setIgnoredOn(taskSave.getIgnoredOn());
		});
	}

	/**
	 * Method to be run any time a chat message is sent.
	 * All tasks will have a chat messages related to them - specifically completion chat messages.
	 * Hook into the chat messages by implementing this method.
	 *
	 * @param chatMessage RuneLite chat message event
	 */
	public abstract void handleChatMessage(ChatMessage chatMessage);

	/**
	 * Method to be run any time a widget is loaded.
	 * Most tasks will have an interface (combat task progress, leagues tab, etc). Data can be scraped from this interface.
	 * Hook into a widget opening by overriding this method.
	 *
	 * @param widgetLoaded RuneLite widget loaded event
	 */
	public void handleOnWidgetLoaded(WidgetLoaded widgetLoaded)
	{
	}

	/**
	 * Method to be run any time after a script has fired.
	 * Most tasks will have a script that load a list of tasks into an interface. Some lists are not accessible through widgets until this script is complete.
	 * Hook into the completion of a script by overriding this method.
	 *
	 * @param scriptPostFired RuneLite post script fired event
	 */
	public void handleOnScriptPostFired(ScriptPostFired scriptPostFired)
	{
	}

	public HashMap<Integer, Integer> getVarbits()
	{
		return new HashMap<>();
	}

	public HashMap<Integer, Integer> getVarps()
	{
		return new HashMap<>();
	}

	public void redraw()
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.redraw());
	}

	public void refresh(Task task)
	{
		SwingUtilities.invokeLater(() -> plugin.pluginPanel.refresh(task));
	}

	public void completeTask(String taskName)
	{
		String processedTaskName = taskName.trim();
		Optional<Task> first = tasks.stream().filter(t -> t.getName().equalsIgnoreCase(processedTaskName)).findFirst();
		first.ifPresent(task -> {
			task.setTracked(false);
			task.setCompleted(true);
			if (plugin.selectedTaskType == taskType)
			{
				refresh(task);
			}
			trackerDataStore.saveTask(task);
		});
	}

	public void updateTaskProgress(LinkedHashMap<String, Boolean> taskProgress)
	{
		// TODO: Hacky, come up with more performant solution & consider case sensitivity
		for (Task task : tasks)
		{
			if (taskProgress.containsKey(task.getName()))
			{
				task.setCompleted(taskProgress.get(task.getName()));
				trackerDataStore.saveTask(task);
			}
		}

		sendTaskUpdateMessage(taskProgress);
	}

	private void sendTaskUpdateMessage(LinkedHashMap<String, Boolean> taskProgress)
	{
		String taskCount = String.valueOf(taskProgress.size());
		String helpMessage = " (remove filters to get full export)";
		Color messageColor = Color.decode("#940B00");
		if (maxTaskCount > 0)
		{
			taskCount += "/" + maxTaskCount;
			if (maxTaskCount == taskProgress.size())
			{
				messageColor = Color.decode("#007517");
				helpMessage = "";
			}
		}
		plugin.sendChatMessage(taskCount + " tasks stored for export" + helpMessage, messageColor);
	}
}
