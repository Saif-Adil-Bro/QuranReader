import re

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "r") as f:
    content = f.read()

daily_message_state = """
    private val _dailyMessageEnabled = MutableStateFlow(false)
    val dailyMessageEnabled: StateFlow<Boolean> = _dailyMessageEnabled.asStateFlow()

    private val _dailyMessageHour = MutableStateFlow(8)
    val dailyMessageHour: StateFlow<Int> = _dailyMessageHour.asStateFlow()

    private val _dailyMessageMinute = MutableStateFlow(0)
    val dailyMessageMinute: StateFlow<Int> = _dailyMessageMinute.asStateFlow()

    fun toggleDailyMessage(enabled: Boolean) {
        _dailyMessageEnabled.value = enabled
        sharedPrefs.edit().putBoolean("daily_message_enabled", enabled).apply()
        val context = repository.context
        if (enabled) {
            com.example.receiver.DailyMessageReceiver.scheduleNextAlarm(context)
        } else {
            com.example.receiver.DailyMessageReceiver.cancelAlarm(context)
        }
    }

    fun updateDailyMessageTime(hour: Int, minute: Int) {
        _dailyMessageHour.value = hour
        _dailyMessageMinute.value = minute
        sharedPrefs.edit()
            .putInt("daily_message_hour", hour)
            .putInt("daily_message_minute", minute)
            .apply()
        
        if (_dailyMessageEnabled.value) {
            com.example.receiver.DailyMessageReceiver.scheduleNextAlarm(repository.context)
        }
    }
"""

# Insert before "fun updatePlannerTarget" or similar
content = content.replace("fun updatePlannerTarget(target: String) {", daily_message_state + "\n    fun updatePlannerTarget(target: String) {")

# Also initialize in init block
init_code = """
        _dailyMessageEnabled.value = sharedPrefs.getBoolean("daily_message_enabled", false)
        _dailyMessageHour.value = sharedPrefs.getInt("daily_message_hour", 8)
        _dailyMessageMinute.value = sharedPrefs.getInt("daily_message_minute", 0)
"""

content = content.replace("_plannerReminderEnabled.value = sharedPrefs.getBoolean(\"planner_reminder\", false)", init_code + "\n        _plannerReminderEnabled.value = sharedPrefs.getBoolean(\"planner_reminder\", false)")

with open("app/src/main/java/com/example/ui/viewmodels/SettingsViewModel.kt", "w") as f:
    f.write(content)
