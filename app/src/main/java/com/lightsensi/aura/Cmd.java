package com.lightsensi.aura;

public class Cmd {

public static final String[] S = {

"1. FPS & 120Hz LOCK", "Refresh rate + frame rate 120 lock", """
for k in vendor.display.refresh_rate user_refresh_rate sf.refresh_rate min_frame_rate max_frame_rate NV_FPSLIMIT tran_refresh_mode last_tran_refresh_mode_in_refresh_setting tran_need_recovery_refresh_mode display_min_refresh_rate min_refresh_rate max_refresh_rate peak_refresh_rate thermal_limit_refresh_rate; do settings put system $k 120; done
for k in user_refresh_rate miui_refresh_rate refresh_rate_mode; do settings put secure $k 120; done
settings put global min_fps 120
settings put global max_fps 120
settings put system fstb_target_fps_margin_high_fps 20
settings put system fstb_target_fps_margin_low_fps 20
settings put system gcc_fps_margin 10
settings put system tran_low_battery_60hz_refresh_rate.support 0
for p in debug.redroid.fps debug.javafx.animation.framerate debug.OVRPlugin.systemDisplayFrequency; do setprop $p 120; done
echo "FPS 120 LOCKED"
""",

"2. LIGHT SENSI TOUCH", "Pointer 7 | 180 events | 1440Hz", """
settings put global windowsmgr.max_events_per_sec 180
settings put system windowsmgr.max_events_per_sec 180
settings put system pointer_speed 7
settings put system vertical_mousd_movement 0.1
settings put system latency_pointer 0.01
settings put system touch_oem true
device_config put input filtered_accel_event_rate_hz 1440
echo "TOUCH SENSI APPLIED"
""",

"3. GPU / RENDER / SF", "SurfaceFlinger | EGL | HWUI skia", """
for kv in high_fps.early.app.duration=10000000 high_fps.early.sf.duration=13800000 high_fps.earlyGl.app.duration=10000000 high_fps.earlyGl.sf.duration=13800000 high_fps.late.app.duration=10000000 high_fps_early_cpu_app_offset_ns=60 high_fps_early_gpu_app_offset_ns=60 early.app.duration=20000000 early.sf.duration=27600000 earlyGl.app.duration=20000000 earlyGl.sf.duration=27600000 hwc.min.duration=23000000 late.app.duration=20000000 late.sf.duration=27600000 high_fps.hwc.min.duration=8500000 high_fps_late_app_phase_offset_ns=100000 phase_offset_threshold_for_next_vsync_ns=6100000 early_phase_offset_ns=500000 early_app_phase_offset_ns=500000 early_gl_phase_offset_ns=3000000 high_fps_early_gl_phase_offset_ns=9000000 early_gl_app_phase_offset_ns=15000000 high_fps_early_phase_offset_ns=6100000 enable_gl_backpressure=0 luma_sampling=1 use_frame_rate_priority=1 disable_backpressure=1 use_phase_offsets_as_durations=0 enable_advanced_sf_phase_offset=1 enable_hwc_vds=0 auto_latch_unsignaled=1 latch_unsignaled=1 showupdates=0 showcpu=0 showbackground=0 showfps=0 vsync=0 hw=1; do setprop debug.sf.${kv%=*} ${kv#*=}; done
for kv in egl.force_ssaa=false egl.force_smaa=false egl.force_taa=false egl.force_msaa=false egl.force_fxaa=false egl.callstack=false egl.swapinterval=90 egl.hw=0 egl.buffcount=4 gr.numframebuffers=3 gr.swapinterval=0 composition.type=skiaglthreaded renderengine.backend=skiaglthreaded hwui.renderer=skiaglthreaded hwui.shadow.renderer=skiaglthreaded hwui.fps_divisor=-1 cpurend.vsync=false mdpcomp.logs=0 gpu.scheduler_pre.emption=1 graphics.gpu.profiler.support=true dev.ssrm.turbo=true display.allow_non_native_refresh_rate_override=1; do setprop debug.${kv%=*} ${kv#*=}; done
settings put system HardwareRenderer 1
settings put system HardwareAccelerated true
settings put global GPUTUNER_SWITCH true
settings put global gpu_render_policy 1
settings put global gpu_composition_policy 1
device_config put systemui window_blur 0
device_config put systemui window_shadow 0
echo "GPU BOOST APPLIED"
""",

"4. CPU / RAM BEAST", "Threads | freezer | priority", """
C=$(grep -c processor /proc/cpuinfo)
setprop debug.rs.max-threads $C
setprop debug.rs.min-threads $C
for kv in systemuicompilerfilter=speed cpuprio=7 gpuprio=7 ioprio=7 performance.profile=1 performance.tuning=1 performance_schema=1 performance.disturb=1 perfmond.atrace=0; do setprop debug.${kv%=*} ${kv#*=}; done
for i in 1 2 3 4; do device_config put activity_manager power_check_max_cpu_$i 0; done
device_config put activity_manager max_empty_time_millis 43200000
device_config put activity_manager_native_boot use_freezer true
device_config put lmkd_native thrashing_limit_critical 200
settings put global fstrim_mandatory_interval 21600000
settings put global cached_apps_freezer 1
echo "CPU BOOST APPLIED - cores: $C"
""",

"5. GAME BOOSTER", "FF | PUBG | CODM | MLBB (dhire chole)", """
for p in com.dts.freefireth com.dts.freefiremax com.tencent.ig com.pubg.krmobile com.garena.codm com.mobile.legends com.mobilelegendshwag com.miHoYo.GenshinImpact jp.konami.pesam com.mojang.minecraftpe com.rockstargames.gtasa com.maleo.bussimulatorid com.netease.newspike com.carxtech.sr com.ea.gp.fifamobile com.kurogame.wutheringwaves.global; do
pm path $p >/dev/null 2>&1 || continue
echo "Boosting $p"
cmd game mode performance $p
dumpsys deviceidle whitelist +$p >/dev/null
cmd package compile -m speed -f $p >/dev/null
done
dumpsys deviceidle whitelist +com.android.systemui >/dev/null
echo "GAME BOOST FINISHED"
""",

"6. CLEANER + LAG KILLER", "Cache trim | kill bg apps", """
rm -rf /sdcard/Android/data/*/cache/* 2>/dev/null
pm trim-caches 999G
cmd looper_stats disable
cmd shortcut reset-all-throttling
am kill-all
echo "CLEAN DONE"
""",

"7. POWER + THERMAL", "Fixed performance | thermal off", """
cmd power set-adaptive-power-saver-enabled false
cmd power set-fixed-performance-mode-enabled true
cmd power set-mode 0
cmd thermalservice override-status 0
settings put global zen_mode 0
echo "POWER UNLOCKED"
""",

"8. CHIP + HWUI AURA CORE", "MTK/QC | RenderScript | logs off", """
for kv in qctwa.preservebuf=1 qctwa.statusbar=1 qc.hardware=true mediatek.appgamepq_compress=1 mediatek.disp_decompress=1 choreographer.skipwarning=1000 choreographer.frametime=false renderthread.reduceopstasksplitting=true bcc.nocache=1 enabletr=true overlayui.enable=1 kill_allocating_task=0 sensor.hal=0 sensors=false stagefright.c2inputsurface=true stagefright.ccodec=true media.vpp.enable=false media.video.frc=false media.video.vpp=false media.video.style=false; do setprop debug.${kv%=*} ${kv#*=}; done
for kv in show_dirty_regions=false capture_skp_enabled=0 profile=false level=0 overdraw=false profile.maxframes=120 render_dirty_regions=false show_layers_updates=false skia_atrace_enabled=false skip_empty_damage=true use_buffer_age=false use_partial_updates=true; do setprop debug.hwui.${kv%=*} ${kv#*=}; done
for kv in debug=0 forcecompat=1 profile=0 reduce=1 script=0 shader=0 visual=0 verbose=0 noprofile=1 nointrinsicblur=1 blurimpl=gpu notextures=1 noobjcache=1 noperfhint=1 force_finish=1 disable_expand=1 use_fast_math=1; do setprop debug.rs.${kv%=*} ${kv#*=}; done
for t in ALL ActivityManager SystemServer PackageManager WindowManager Telephony PowerManager dalvikvm ConnectivityService WifiService Bluetooth SensorService AudioFlinger MediaPlayer SurfaceFlinger Zygote; do setprop log.tag.$t -; done
echo "AURA CORE APPLIED"
"""
};

public static final String[] X = {

"9. EXTREME (RISKY)", "GMS block | notification late hote pare", """
device_config put activity_manager max_phantom_processes 1
device_config put activity_manager max_cached_processes 1
device_config put activity_manager max_background_processes 1
device_config put activity_manager foreground_service_max_memory_kb 500
device_config put activity_manager background_service_max_memory_kb 250
for g in com.google.android.gms com.google.android.ims; do
for op in RUN_IN_BACKGROUND RUN_ANY_IN_BACKGROUND START_FOREGROUND INSTANT_APP_START_FOREGROUND; do cmd appops set $g $op ignore; done
done
for c in nearby.messages.service.NearbyMessagesService nearby.sharing.service.NearbySharingService perfprofile.uploader.PerfProfileCollectorService photos.autobackup.service.AutoBackupService; do
pm disable com.google.android.gms/.$c
done
am force-stop com.google.android.ims
cmd activity kill-all
echo "EXTREME APPLIED"
""",

"10. RESET ALL", "Somossa hole sob tweak undo", """
for k in fstb_target_fps_margin_high_fps fstb_target_fps_margin_low_fps gcc_fps_margin vendor.display.refresh_rate user_refresh_rate sf.refresh_rate min_frame_rate max_frame_rate NV_FPSLIMIT tran_refresh_mode display_min_refresh_rate min_refresh_rate max_refresh_rate peak_refresh_rate thermal_limit_refresh_rate windowsmgr.max_events_per_sec vertical_mousd_movement latency_pointer touch_oem HardwareRenderer HardwareAccelerated; do settings delete system $k; done
settings put system pointer_speed 0
for k in user_refresh_rate miui_refresh_rate refresh_rate_mode; do settings delete secure $k; done
for k in min_fps max_fps windowsmgr.max_events_per_sec GPUTUNER_SWITCH gpu_render_policy gpu_composition_policy fstrim_mandatory_interval; do settings delete global $k; done
for k in max_phantom_processes max_cached_processes max_background_processes max_empty_time_millis foreground_service_max_memory_kb background_service_max_memory_kb power_check_max_cpu_1 power_check_max_cpu_2 power_check_max_cpu_3 power_check_max_cpu_4; do device_config delete activity_manager $k; done
device_config delete input filtered_accel_event_rate_hz
device_config delete lmkd_native thrashing_limit_critical
for g in com.google.android.gms com.google.android.ims; do
for op in RUN_IN_BACKGROUND RUN_ANY_IN_BACKGROUND START_FOREGROUND INSTANT_APP_START_FOREGROUND; do cmd appops set $g $op default; done
done
for c in nearby.messages.service.NearbyMessagesService nearby.sharing.service.NearbySharingService perfprofile.uploader.PerfProfileCollectorService photos.autobackup.service.AutoBackupService; do
pm enable com.google.android.gms/.$c
done
cmd power set-fixed-performance-mode-enabled false
cmd thermalservice reset
echo "RESET DONE - reboot dile baki gula default hobe"
""",

"11. DEVICE INFO", "Model | SDK | Kernel | GPU", """
echo "Kernel   : $(uname -r)"
echo "SDK      : $(getprop ro.build.version.sdk)"
echo "Android  : $(getprop ro.build.version.release)"
echo "Model    : $(getprop ro.product.model)"
echo "Brand    : $(getprop ro.product.brand)"
echo "Hardware : $(getprop ro.hardware)"
echo "GLES     : $(getprop ro.opengles.version)"
echo "CPU ABI  : $(getprop ro.product.cpu.abi)"
"""
};
}
