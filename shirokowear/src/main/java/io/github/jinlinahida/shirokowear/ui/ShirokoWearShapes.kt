package io.github.jinlinahida.shirokowear.ui

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/** Corner geometry shared by cards, buttons and sheets. */
public object ShirokoWearShapes {
    /** The card radius that defines the "ink slate" look; hairline border sits on top of it. */
    public val card: Shape = RoundedCornerShape(10.dp)

    public val cardCompact: Shape = RoundedCornerShape(8.dp)

    public val circle: Shape = CircleShape
}
