package io.github.ronjunevaldoz.awake.core.graphics

import kotlinx.cinterop.useContents
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.CGContextFillRect
import platform.CoreGraphics.CGContextRef
import platform.CoreGraphics.CGContextSetFillColorWithColor
import platform.CoreGraphics.CGDataProviderCopyData
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectGetHeight
import platform.CoreGraphics.CGRectGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSAttributedString
import platform.Foundation.NSData
import platform.Foundation.NSDictionary
import platform.Foundation.NSMutableDictionary
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.Foundation.setValue
import platform.UIKit.NSFontAttributeName
import platform.UIKit.NSForegroundColorAttributeName
import platform.UIKit.NSKernAttributeName
import platform.UIKit.NSLayoutManager
import platform.UIKit.NSLigatureAttributeName
import platform.UIKit.NSLineBreakByTruncatingTail
import platform.UIKit.NSTextContainer
import platform.UIKit.NSTextStorage
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.drawInRect
import platform.UIKit.size
import kotlin.math.ceil


/**
 *
 *  Creates and returns a UIImage by using a custom drawing block on a CGContextRef.
 *  ```kotlin
 *  // example usage
 *  val myImage = createImage(100.0, 100.0) {
 *      CGContextSetFillColorWithColor(this, UIColor.clearColor.CGColor)
 *      CGContextFillRect(this, CGRectMake(0.0, 0.0, width, height))
 *  }
 * ```
 *  @param width The width of the image.
 *  @param height The height of the image.
 *  @param block The drawing block that defines the content of the image.
 *
 *  @return The UIImage generated from the custom drawing block.
 *
 */
fun createImage(width: Double, height: Double, block: CGContextRef.() -> Unit): UIImage {
    UIGraphicsBeginImageContextWithOptions(CGSizeMake(width, height), false, 0.0)
    UIGraphicsGetCurrentContext()?.let(block)
    val image = UIGraphicsGetImageFromCurrentImageContext()!!
    UIGraphicsEndImageContext()
    return image
}


/**
 * TODO Experimental native image
 */
fun createImage(text: String, fontSize: Double): UIImage {
    val adjustedFontSize = fontSize.coerceIn(8.0, 500.0)
    val font = UIFont.systemFontOfSize(fontSize)
    val color = UIColor.blackColor
    // Set up text attributes for styling the rendered text
    val textAttributes = mapOf<Any?, Any>(
        NSFontAttributeName to font,
        NSForegroundColorAttributeName to color,
        NSKernAttributeName to 0.0f,
        NSLigatureAttributeName to 0
    )

    // Create attributed string with the specified text and attributes
    val textString = NSAttributedString.create(text, textAttributes)
    // Create text storage and add the attributed string
    val textStorage = NSTextStorage.create(textString)
    val textSize = textString.size()
//    val rect  = textString.boundingRectWithSize(
//        size = CGSizeMake(Double.MAX_VALUE, Double.MAX_VALUE),
//        options = NSStringDrawingUsesLineFragmentOrigin,
//        context = null
//    )
//
//    val width = ceil(CGRectGetWidth(rect))
//    val height = ceil(CGRectGetHeight(rect))

    // Create text container with fixed size for text layout
    val textContainer = NSTextContainer(CGSizeMake(48.0, 48.0 + 2))

    // Create layout manager and add the text container
    val layoutManager = NSLayoutManager()
    layoutManager.addTextContainer(textContainer)
    textStorage.addLayoutManager(layoutManager)

    textContainer.apply {
        // Configure text container properties
        lineFragmentPadding = 0.0
        lineBreakMode = NSLineBreakByTruncatingTail
        maximumNumberOfLines = 1u
    }

    // Get the bounding rectangle of the rendered text
//    val textRect = layoutManager.usedRectForTextContainer(textContainer)

    val textRect = textSize.useContents { CGRectMake(0.0, 0.0, width, height) }
    val width = ceil(CGRectGetWidth(textRect))
    val height = ceil(CGRectGetHeight(textRect))
//        // Create the image and draw the text on the context
    return createImage(width, height) {
        CGContextSetFillColorWithColor(this, UIColor.clearColor.CGColor)
        CGContextFillRect(this, CGRectMake(0.0, 0.0, width, height))

        // Calculate the origin for drawing the text centered vertically
        val textOrigin = CGPointMake(0.0, (height - adjustedFontSize) / 2)
        // Draw the glyphs of the text on the context
        layoutManager.drawGlyphsForGlyphRange(
            glyphsToShow = layoutManager.glyphRangeForTextContainer(textContainer),
            atPoint = textOrigin
        )
    }
}

// Extension function to convert Map to NSDictionary
fun <K, V> Map<K, V>.toNSDictionary(): NSDictionary {
    val nsDictionary = NSMutableDictionary()
    this.forEach { (key, value) ->
        nsDictionary.setValue(value, key.toString())
    }
    return nsDictionary
}

fun getCharacterGlyph(character: Char, fontSize: Float): UIImage? {
    val font = UIFont.systemFontOfSize(fontSize.toDouble())
    val attributes = mapOf<Any?, Any>(
        NSFontAttributeName to font,
        NSForegroundColorAttributeName to UIColor.blackColor,
        NSKernAttributeName to 0.0f,
        NSLigatureAttributeName to 0
    )
    val attributedString = NSAttributedString.create(character.toString(), attributes)
    val size = attributedString.size()
    UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
    val rect = size.useContents {
        CGRectMake(0.0, 0.0, width, height)
    }
    attributedString.drawInRect(rect)
    val image = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return image
}

fun UIImage.toNSData(): NSData {
    val dataProvider = CGDataProviderCopyData(CGImageGetDataProvider(this.CGImage))
    val length = CFDataGetLength(dataProvider)
    val nsData = NSData.dataWithBytes(dataProvider, length.toULong())
    CFRelease(dataProvider)
    return nsData
}
