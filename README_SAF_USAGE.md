# Fire OS SAF USB Access - Usage Guide

## 🎯 SAF Implementation for Fire OS USB Access

This app uses Android's Storage Access Framework (SAF) to access USB drives on Fire OS, which has strict storage restrictions.

## 📁 Directory Structure

Your USB drive should contain video files organized like this:

```
📁 Your USB Drive Root (select this folder in SAF picker)
   ├── 📁 Common/     (videos played 60% of time)
   │   ├── video1.mp4
   │   └── video2.mp4
   ├── 📁 Uncommon/   (videos played 25% of time)
   │   ├── video3.mp4
   ├── 📁 Rare/       (videos played 10% of time)
   │   └── video4.mp4
   └── 📁 Legendary/  (videos played 5% of time)
       └── video5.mp4
```

## 🧪 Test Directory Structure

A test directory structure has been created in the project:

```
📁 app/test_usb/
   ├── 📁 Common/
   ├── 📁 Uncommon/
   ├── 📁 Rare/
   └── 📁 Legendary/
```

## 📱 Usage Steps

1. **Launch App** → Shows "Please select USB drive"
2. **Tap "SELECT USB" Button** → Android file picker opens
3. **Navigate to USB Drive** → Select the root folder containing Common/Uncommon/Rare/Legendary
4. **Grant Permissions** → Allow read access to selected folder
5. **Video Playback** → Videos play automatically with weighted random selection

## 🔧 SAF Picker Issues & Solutions

### Problem: "Only create folders" visible
**Solutions tried:**
- Set INITIAL_URI to external storage root
- Enabled advanced picker options
- Added SHOW_FILESIZE extra

### Alternative Approaches:
- **Manual Directory**: Place videos directly in USB root if navigation doesn't work
- **Nested Structure**: If needed, videos can be in `USB_ROOT/Movies/Common/` etc.

## 🚀 Fire OS Compatibility

This SAF implementation:
- ✅ Works around Fire OS "All Files Access" permission restrictions
- ✅ Uses document-based access instead of direct filesystem paths
- ✅ Persists permissions across app restarts
- ✅ Monitors USB availability and automatically reconnects
- ✅ Supports weighted random video selection

## 📊 Status Monitoring

The app provides real-time status:
- "Please select USB drive" - No USB configured
- "Looking for USB drive..." - SAF picker active
- "USB connected and ready" - USB found, videos scanned
- "USB drive disconnected" - USB removed
- Video playback status messages

## 🔄 Automatic Features

- **Persistent Permissions**: USB access survives app restarts
- **Background Monitoring**: Detects USB removal/insertion every 4 seconds
- **Auto-Reconnection**: Resumes playback when USB returns
- **Error Recovery**: Handles SAF permission loss gracefully

## 🐛 Troubleshooting

### USB Not Detected
- Ensure videos are in Common/Uncommon/Rare/Legendary folders
- Try selecting a higher-level folder in the directory structure
- Check Android logs for SAF permission errors

### Permission Issues
- SAF permissions must be granted each time the folder selection changes
- Fire OS may restrict which folders can be selected via SAF
- Check device settings for storage permission restrictions
