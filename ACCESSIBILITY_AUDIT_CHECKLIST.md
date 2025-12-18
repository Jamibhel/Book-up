# Accessibility Audit Checklist - BookUp

**Status**: 🔍 AUDIT IN PROGRESS  
**Target**: WCAG 2.1 Level AA Compliance  
**Date**: December 18, 2025

---

## Quick Reference

| Category | Priority | Status |
|----------|----------|--------|
| **Color Contrast** | 🔴 CRITICAL | ⏳ |
| **Touch Targets** | 🔴 CRITICAL | ⏳ |
| **Content Descriptions** | 🔴 CRITICAL | ⏳ |
| **Text Size** | 🟠 HIGH | ⏳ |
| **Focus Indicators** | 🟠 HIGH | ⏳ |
| **Dark Mode** | 🟡 MEDIUM | ✅ READY |
| **Keyboard Navigation** | 🟠 HIGH | ⏳ |
| **Form Labels** | 🟠 HIGH | ⏳ |

---

## 1. COLOR CONTRAST AUDIT

### WCAG Standards
- **Normal text**: Minimum 4.5:1 contrast ratio
- **Large text** (18sp+ or 14sp+ bold): Minimum 3:1 contrast ratio
- **Graphics/UI components**: Minimum 3:1 contrast ratio

### Light Mode Color Pairs (Check Contrast)

| Element | Foreground | Background | Ratio | Pass? | Fix |
|---------|-----------|-----------|-------|-------|-----|
| Primary Button Text | #FFFFFF | #2E8B57 (primary) | ? | ⏳ | Check |
| Body Text | #000000 | #FFFFFF | ? | ⏳ | Check |
| Secondary Text | #666666 | #FFFFFF | ? | ⏳ | Check |
| Input Border | #2E8B57 | #FFFFFF | ? | ⏳ | Check |
| Disabled Text | #CCCCCC | #FFFFFF | ? | ⏳ | Check |
| Error Text | #EF4444 | #FFFFFF | ? | ⏳ | Check |
| Link Text | #1B9A8B (secondary) | #FFFFFF | ? | ⏳ | Check |
| Card Shadow Text | #000000 | #F5F5F5 | ? | ⏳ | Check |
| Message Incoming | #000000 | #F5F5F5 | ? | ⏳ | Check |
| Message Outgoing | #FFFFFF | #2E8B57 | ? | ⏳ | Check |

### Dark Mode Color Pairs (Check Contrast)

| Element | Foreground | Background | Ratio | Pass? | Fix |
|---------|-----------|-----------|-------|-------|-----|
| Primary Button Text | #0F3D27 | #52B788 | ? | ⏳ | Check |
| Body Text | #E7E8EB | #0F1419 | ? | ⏳ | Check |
| Secondary Text | #A0A0A0 | #0F1419 | ? | ⏳ | Check |
| Input Border | #52B788 | #131820 | ? | ⏳ | Check |
| Disabled Text | #666666 | #0F1419 | ? | ⏳ | Check |
| Error Text | #FF6B6B | #0F1419 | ? | ⏳ | Check |
| Link Text | #40C4B4 (secondary) | #0F1419 | ? | ⏳ | Check |
| Card Surface | #E7E8EB | #131820 | ? | ⏳ | Check |
| Message Incoming | #E7E8EB | #0F6B63 | ? | ⏳ | Check |
| Message Outgoing | #E7E8EB | #1B5E3F | ? | ⏳ | Check |

### Tools to Check Contrast
- WebAIM Contrast Checker: https://webaim.org/resources/contrastchecker/
- Color Contrast Analyzer by TPGi
- Android Studio Built-in Accessibility Scanner

---

## 2. TOUCH TARGET SIZE AUDIT

### WCAG Guidelines
- **Minimum**: 48dp × 48dp (Material Design standard)
- **Preferred**: 56dp × 56dp
- **Spacing**: At least 8dp between touch targets

### Screen by Screen Audit

### SignInActivity / RegisterActivity
```
ELEMENTS TO CHECK:
- [ ] Login button: 40dp height? → Should be 48dp minimum
- [ ] Password field: 56dp height? ✅ (Material standard)
- [ ] Toggle password visibility: 48dp × 48dp? 
- [ ] "Forgot Password" link: 48dp clickable area?
- [ ] "Sign Up" text link: 48dp clickable area?
- [ ] Spacing between fields: 8dp minimum?

FIXES NEEDED:
- [ ] Increase button to 48dp height
- [ ] Add padding around text links (invisible clickable area)
- [ ] Ensure 8dp gap between all interactive elements
```

### ProfileFragment / ProfileEditActivity
```
ELEMENTS TO CHECK:
- [ ] Edit button: 48dp × 48dp?
- [ ] Delete account button: 48dp height?
- [ ] Save changes button: 48dp height?
- [ ] Avatar: 56dp × 56dp? (should be for interaction)
- [ ] Text fields: 56dp height?

FIXES NEEDED:
- [ ] Make edit button 48dp
- [ ] All buttons should be 48dp+ height
- [ ] Ensure adequate spacing
```

### ChatActivity
```
ELEMENTS TO CHECK:
- [ ] Send button: 48dp × 48dp?
- [ ] Attachment button: 48dp × 48dp?
- [ ] Menu items: 48dp × 48dp?
- [ ] Avatar in list: 40dp minimum?
- [ ] Message bubbles: Tappable area 48dp height?

FIXES NEEDED:
- [ ] Increase send button to 48dp
- [ ] Make attachment button 48dp
- [ ] Add adequate padding around message items
```

### TutorDetailsActivity
```
ELEMENTS TO CHECK:
- [ ] Message button: 48dp height?
- [ ] Book session button: 48dp height?
- [ ] Leave review button: 48dp height?
- [ ] Rating stars: 48dp × 48dp interactive?
- [ ] Subject chips: 48dp minimum height?

FIXES NEEDED:
- [ ] All action buttons 48dp height
- [ ] Add padding around chips
- [ ] Make rating stars easily tappable
```

### BookingSessionActivity
```
ELEMENTS TO CHECK:
- [ ] Date picker button: 48dp × 48dp?
- [ ] Time picker button: 48dp × 48dp?
- [ ] Book button: 48dp height?
- [ ] Cancel button: 48dp height?
- [ ] Text fields: 56dp height?

FIXES NEEDED:
- [ ] All buttons 48dp height minimum
- [ ] Date/time pickers 48dp
- [ ] Proper spacing between fields
```

---

## 3. CONTENT DESCRIPTIONS AUDIT

### WCAG Requirement
**Every non-text element must have a content description**

### Images to Check

#### SignInActivity
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] Logo ImageView: android:contentDescription="@string/app_logo"
- [ ] Background image: android:contentDescription="@string/app_background"
- [ ] Password visibility toggle: android:contentDescription="@string/toggle_password"
```

#### ProfileFragment / ProfileEditActivity
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] User avatar: android:contentDescription="@string/user_profile_photo"
- [ ] Edit icon: android:contentDescription="@string/edit_profile"
- [ ] Delete icon: android:contentDescription="@string/delete_account"
- [ ] Close button: android:contentDescription="@string/close"
- [ ] Background image: android:contentDescription="@string/profile_background"
```

#### ChatActivity
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] User avatars in list: android:contentDescription="@{message.userName}"
- [ ] Attachment icon: android:contentDescription="@string/attach_file"
- [ ] Send button: android:contentDescription="@string/send_message"
- [ ] Menu icon: android:contentDescription="@string/menu"
- [ ] Profile click area: android:contentDescription="@{tutorName}"
```

#### TutorDetailsActivity
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] Tutor photo: android:contentDescription="@string/tutor_profile_photo"
- [ ] Rating stars: android:contentDescription="@{ratingCount} @string/ratings"
- [ ] Verified badge: android:contentDescription="@string/verified_tutor"
- [ ] Subject icons: android:contentDescription="@{subject}"
- [ ] Action buttons: Clear descriptions for Book, Message, Review
```

#### ReviewsBottomSheetFragment
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] Rating stars: android:contentDescription="@string/select_rating"
- [ ] Submit button: android:contentDescription="@string/submit_review"
- [ ] Close button: android:contentDescription="@string/close"
```

#### BookingSessionActivity
```xml
<!-- NEED CONTENT DESCRIPTIONS FOR: -->
- [ ] Calendar icon: android:contentDescription="@string/select_date"
- [ ] Clock icon: android:contentDescription="@string/select_time"
- [ ] Submit button: android:contentDescription="@string/book_session"
- [ ] Cancel button: android:contentDescription="@string/cancel"
```

### Implementation Pattern
```xml
<!-- For images that are purely decorative (hidden from screen readers) -->
<ImageView
    android:contentDescription="@null"
    android:importantForAccessibility="no" />

<!-- For meaningful images -->
<ImageView
    android:src="@drawable/ic_profile"
    android:contentDescription="@string/user_profile_photo"
    android:contentDescription="@{userProfilePhotoDescription}" />

<!-- For buttons with just an icon -->
<ImageButton
    android:src="@drawable/ic_edit"
    android:contentDescription="@string/edit_profile"
    android:background="?attr/selectableItemBackgroundBorderless" />
```

---

## 4. TEXT SIZE AUDIT

### WCAG Guidelines
- **Body text**: Minimum 14sp (12pt)
- **Heading**: Minimum 18sp (14pt) or 14sp bold
- **UI controls**: Minimum 14sp
- **Support**: Users can zoom to 200% without horizontal scroll

### Text Size Verification by Screen

| Screen | Text Element | Current Size | Recommended | Status |
|--------|-------------|--------------|-------------|--------|
| SignIn | Body | ? | 14sp | ⏳ |
| SignIn | Heading | ? | 24sp-32sp | ⏳ |
| SignIn | Button | ? | 14sp | ⏳ |
| Profile | Name | ? | 24sp-28sp | ⏳ |
| Profile | Bio | ? | 14sp | ⏳ |
| Profile | Labels | ? | 12sp | ⏳ |
| Chat | Messages | ? | 16sp | ⏳ |
| Chat | Timestamps | ? | 12sp | ⏳ |
| Details | Title | ? | 28sp-32sp | ⏳ |
| Details | Rating | ? | 14sp | ⏳ |
| Details | Description | ? | 14sp | ⏳ |
| Booking | Title | ? | 28sp-32sp | ⏳ |
| Booking | Labels | ? | 12sp | ⏳ |
| Booking | Fields | ? | 14sp | ⏳ |
| Review | Title | ? | 24sp-28sp | ⏳ |
| Review | Input | ? | 14sp | ⏳ |

---

## 5. FOCUS INDICATORS AUDIT

### WCAG Requirement
**All interactive elements must have visible focus indicators**

### Implementation

#### In styles.xml
```xml
<!-- Add focus state to all interactive elements -->
<style name="Widget.BookUp.Button.Focused">
    <item name="android:background">?attr/selectableItemBackground</item>
    <item name="android:foreground">?attr/selectableItemBackgroundBorderless</item>
</style>
```

#### In layout XML
```xml
<!-- Method 1: Use Material Button (has built-in focus) -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.Material3.Button"
    android:focusable="true"
    android:focusableInTouchMode="false" />

<!-- Method 2: Add explicit focus ring -->
<View
    android:id="@+id/focus_ring"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorOnBackground"
    android:alpha="0.2"
    android:visibility="gone" />

<!-- Method 3: Use shape drawable for focus ring -->
<layer-list>
    <item>
        <shape>
            <stroke
                android:width="2dp"
                android:color="?attr/colorPrimary" />
        </shape>
    </item>
</layer-list>
```

### Elements to Verify Focus
- [ ] All buttons have visible focus ring
- [ ] All input fields highlight on focus
- [ ] All links show focus state
- [ ] All clickable items show focus state
- [ ] Focus ring is clearly visible (contrast 3:1+)
- [ ] Focus order is logical (top to bottom, left to right)

---

## 6. KEYBOARD NAVIGATION AUDIT

### WCAG Requirement
**All functionality must be accessible via keyboard**

### Checklist

#### SignInActivity
- [ ] Can tab through all fields in logical order
- [ ] Can submit form with Enter key
- [ ] Password visibility toggle accessible via keyboard
- [ ] Links are keyboard accessible
- [ ] Back button accessible

#### ChatActivity
- [ ] Can tab through all messages
- [ ] Can select/interact with message bubbles
- [ ] Send button accessible via keyboard
- [ ] Attachment button accessible
- [ ] Can navigate to tutor profile via keyboard
- [ ] Input field fully accessible

#### TutorDetailsActivity
- [ ] Can tab through all info
- [ ] Can focus and activate all buttons
- [ ] Can interact with rating display
- [ ] Can navigate back via keyboard

#### BookingSessionActivity
- [ ] Can access all form fields
- [ ] Date picker accessible
- [ ] Time picker accessible
- [ ] Can submit form
- [ ] Can cancel via keyboard

### Testing Method
```
1. Disable touchscreen in developer settings
2. Use keyboard only to navigate app
3. Ensure all functions are accessible
4. Verify logical tab order
```

---

## 7. FORM LABELS AUDIT

### WCAG Requirement
**All form inputs must have associated labels**

### Implementation Pattern

```xml
<!-- CORRECT: Associated label with input via android:labelFor -->
<LinearLayout>
    <TextView
        android:id="@+id/subject_label"
        android:text="@string/subject"
        android:textAppearance="@style/TextAppearance.BookUp.LabelLarge" />
    
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/subject_input"
        android:labelFor="@+id/subject_edit"
        app:hint="@string/subject_hint"
        app:helperText="@string/subject_helper">
        
        <EditText
            android:id="@+id/subject_edit"
            android:inputType="text"
            android:contentDescription="@string/subject_field" />
    </com.google.android.material.textfield.TextInputLayout>
</LinearLayout>
```

### All Screens

#### SignInActivity
- [ ] Email field has label "Email" via android:labelFor
- [ ] Password field has label "Password"
- [ ] Checkbox has descriptive label

#### ProfileEditActivity
- [ ] All form fields labeled
- [ ] Bio field labeled and described
- [ ] Photo field labeled

#### ChatActivity
- [ ] Message input labeled

#### BookingSessionActivity
- [ ] Date field labeled
- [ ] Time field labeled
- [ ] Subject field labeled
- [ ] Description field labeled

#### ReviewsBottomSheetFragment
- [ ] Rating labeled "Rating"
- [ ] Comment field labeled "Comment"

---

## 8. DARK MODE SUPPORT AUDIT

### Status: ✅ MOSTLY READY

**Files Created**:
- ✅ `res/values-night/colors.xml` - Dark theme colors
- ✅ `res/values/colors.xml` - Light theme colors
- ✅ Themes configured in `AndroidManifest.xml`

### Checklist

#### Colors
- [x] Dark theme colors defined
- [x] Primary colors set for dark mode
- [x] Secondary colors set for dark mode
- [x] Text colors adapted
- [ ] Test all colors in dark mode

#### Components
- [ ] All TextViews use `?attr/colorOnBackground` not hardcoded black
- [ ] All backgrounds use `?attr/colorBackground` or `?attr/colorSurface`
- [ ] All images visible in both themes
- [ ] All buttons readable in dark mode

#### Testing
- [ ] Tested on Android 12+ (Settings > Display > Dark theme)
- [ ] All screens readable in dark mode
- [ ] No white text on light backgrounds
- [ ] Sufficient contrast maintained

---

## 9. SCREEN READER SUPPORT (TalkBack)

### WCAG Requirement
**App must work with TalkBack screen reader**

### Setup Testing
1. Enable TalkBack: Settings > Accessibility > TalkBack
2. Enable on
3. Navigate with gestures or keyboard

### Checklist

#### All Screens
- [ ] All interactive elements announced
- [ ] Content descriptions read aloud
- [ ] Form labels announced before inputs
- [ ] Buttons announced with action
- [ ] Images announced or skipped appropriately
- [ ] Focus order logical
- [ ] No empty announcements

#### Specific Elements
- [ ] Rating stars announced: "Rating, 1 of 5" etc.
- [ ] Buttons announced: "Book session, button"
- [ ] Chips announced: "Mathematics, chip, double tap to activate"
- [ ] Fields announced: "Subject, edit text, required"

### Fix Pattern
```xml
<!-- Add view accessibility information -->
<Button
    android:accessibilityHeading="true"
    android:contentDescription="@string/book_session_button"
    android:accessibilityLiveRegion="polite" />

<!-- Make decorative elements ignored -->
<ImageView
    android:contentDescription="@null"
    android:importantForAccessibility="no" />

<!-- Announce custom states -->
<View
    android:accessibilityLiveRegion="assertive"
    android:importantForAccessibility="yes" />
```

---

## 10. DYSLEXIA & COGNITIVE ACCESSIBILITY

### Considerations

- [ ] Simple, clear language
- [ ] Short sentences and paragraphs
- [ ] Clear visual hierarchy
- [ ] Consistent navigation patterns
- [ ] Avoid color coding as only indicator
- [ ] Use icons + text together
- [ ] Clear error messages
- [ ] Adequate whitespace

### Implementation

```xml
<!-- Use sans-serif fonts (more readable for dyslexic users) -->
<style name="TextAppearance.BookUp.Body" parent="...">
    <item name="fontFamily">@font/nunito_font_family</item>
    <item name="android:fontFamily">@font/nunito_font_family</item>
</style>

<!-- Adequate line spacing -->
<TextView
    android:lineSpacingExtra="2dp" />

<!-- Clear visual hierarchy -->
<TextView
    android:textSize="24sp"
    android:textStyle="bold"
    android:text="Main Title" />
```

---

## 11. COLOR BLINDNESS ACCESSIBILITY

### Considerations

- [ ] Never use color alone to convey information
- [ ] Use patterns, icons, or text in addition to color
- [ ] Sufficient luminance contrast (not just color difference)

### Implementation Examples

```xml
<!-- WRONG: Conveying status with color only -->
<View
    android:background="@color/success_green" />

<!-- CORRECT: Use icon + text + color -->
<LinearLayout>
    <ImageView
        android:src="@drawable/ic_check_green"
        android:contentDescription="Success" />
    
    <TextView
        android:text="@string/status_success"
        android:textColor="@color/success_green" />
</LinearLayout>

<!-- Error field - show icon, border, and text -->
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/email_input"
    app:error="@string/invalid_email">
    
    <EditText
        android:inputType="text"
        android:drawableEnd="@drawable/ic_error_red"
        android:contentDescription="@string/email_error" />
</com.google.android.material.textfield.TextInputLayout>
```

---

## 12. MOTION & ANIMATION ACCESSIBILITY

### Considerations

- [ ] Animations optional (not required for functionality)
- [ ] No auto-playing videos/animations with sound
- [ ] Respect `android:prefers Reduced Motion` setting (Android 10+)
- [ ] No rapid flashing (avoid >3 flashes per second)

### Implementation

```kotlin
// Respect reduced motion preference
val animator = ValueAnimator.ofFloat(0f, 1f).apply {
    duration = if (context.isReducedMotionEnabled()) 0L else 300L
}

// Or use AnimatorSet with reduced duration
val animSet = AnimatorSet().apply {
    duration = if (reducedMotion) 0L else 300L
}
```

---

## Implementation Priorities

### 🔴 CRITICAL - Fix First (Week 1)

1. **Color Contrast** - WCAG AA compliance required
   - [ ] Check all text pairs
   - [ ] Fix failing contrasts
   - Effort: 2-3 hours

2. **Touch Target Size** - Accessibility requirement
   - [ ] Audit all buttons/clickable areas
   - [ ] Increase to 48dp minimum
   - Effort: 3-4 hours

3. **Content Descriptions** - Screen reader requirement
   - [ ] Add descriptions to all images
   - [ ] Add labels to all inputs
   - Effort: 2-3 hours

### 🟠 HIGH - Fix Soon (Week 1-2)

4. **Text Size** - Readability requirement
   - [ ] Verify all text meets minimums
   - [ ] Adjust where needed
   - Effort: 1-2 hours

5. **Focus Indicators** - Keyboard navigation
   - [ ] Add focus rings to interactive elements
   - [ ] Verify logical tab order
   - Effort: 2-3 hours

6. **Form Labels** - Accessibility requirement
   - [ ] Associate labels with inputs
   - [ ] Verify screen reader announces
   - Effort: 1-2 hours

### 🟡 MEDIUM - Polish (Week 2)

7. **Dark Mode** - Nice to have but important
   - [ ] Test all screens
   - [ ] Fix color visibility issues
   - Effort: 1-2 hours

8. **Keyboard Navigation** - Important for some users
   - [ ] Test without touchscreen
   - [ ] Fix navigation issues
   - Effort: 1-2 hours

---

## Testing Tools

### Automated Tools
- **Android Accessibility Scanner** (built-in)
- **WebAIM Contrast Checker**
- **Color Blindness Simulator**
- **Android Studio Lint Checks**

### Manual Testing
- **TalkBack** (Settings > Accessibility > TalkBack)
- **Keyboard navigation** (disable touchscreen)
- **Visual inspection** (contrast, size, focus)
- **Dark mode** (Settings > Display > Dark theme)

### Command-line Tools
```bash
# Run accessibility checks
adb shell cmd uiautomator dump /tmp/uidump.xml

# Check content descriptions
grep -r "android:contentDescription" app/src/main/res/layout/

# Find hardcoded colors
grep -r "#[0-9A-Fa-f]\{6\}" app/src/main/res/layout/
```

---

## Success Criteria

| Metric | Target | Status |
|--------|--------|--------|
| **Color Contrast** | 100% of text ≥4.5:1 (AA) | ⏳ |
| **Touch Targets** | 100% of buttons ≥48dp × 48dp | ⏳ |
| **Content Descriptions** | 100% of images | ⏳ |
| **Text Size** | 100% of text ≥14sp body, ≥18sp headers | ⏳ |
| **Focus Indicators** | 100% of interactive elements | ⏳ |
| **Keyboard Access** | 100% of functions keyboard accessible | ⏳ |
| **Dark Mode** | 100% of screens readable | ⏳ |
| **TalkBack Support** | All screens navigable | ⏳ |
| **WCAG Compliance** | Level AA or better | ⏳ |

---

## Quick Fix Checklist (Next 3 Hours)

- [ ] Create `dimens.xml` with standard sizes ✅ DONE
- [ ] Create `styles.xml` with component styles ✅ DONE
- [ ] Create `colors-night.xml` for dark mode ✅ DONE
- [ ] Audit 1 screen for contrast issues (SignIn)
- [ ] Add content descriptions to SignInActivity
- [ ] Verify button sizes in SignInActivity
- [ ] Test dark mode on SignInActivity
- [ ] Build and verify no errors

---

**Next Action**: Pick one screen and audit it completely (e.g., SignInActivity), then roll out fixes to other screens. 🎯

