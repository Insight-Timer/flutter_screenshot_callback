package com.flutter.moum.screenshot_callback

import android.os.Handler
import android.os.Looper
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler

class ScreenshotCallbackPlugin :
  FlutterPlugin, MethodCallHandler {
  private var _handler: Handler? = null
  private var _detector: ScreenshotDetector? = null
  private var _lastScreenshotName: String? = null
  private lateinit var _channel: MethodChannel
  private lateinit var _binding: FlutterPlugin.FlutterPluginBinding

  override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
    when (call.method) {
        "initialize" -> {
          _handler = Handler(Looper.getMainLooper())
          _detector = ScreenshotDetector(_binding.applicationContext) { screenshotName: String ->
            if (screenshotName != _lastScreenshotName) {
              _lastScreenshotName = screenshotName
              _handler?.post {
                _channel.invokeMethod("onCallback", null)
              }
            }
          }
          _detector?.start()
          result.success("initialize")
        }
        "dispose" -> {
          _detector?.stop()
          _detector = null
          _lastScreenshotName = null
          result.success("dispose")
        }
        else -> {
          result.notImplemented()
        }
    }
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    _binding = binding
    _channel = MethodChannel(binding.binaryMessenger, "flutter.moum/screenshot_callback")
    _channel.setMethodCallHandler(this)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    _channel.setMethodCallHandler(null)
  }
}