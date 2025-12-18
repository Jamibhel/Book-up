# Modern UI/UX & Accessibility Implementation Guide

**Phase**: Design System & Modernization  
**Status**: 🚀 READY TO IMPLEMENT  
**Date**: December 18, 2025  
**Target**: Material Design 3 + WCAG 2.1 AA Accessibility

---

## What We've Set Up ✅

### 1. Design System Foundation
| File | Purpose | Status |
|------|---------|--------|
| `res/values/dimens.xml` | 80+ standard dimensions | ✅ READY |
| `res/values/styles.xml` | 25+ reusable styles | ✅ READY |
| `res/values/colors.xml` | Light theme colors | ✅ READY |
| `res/values-night/colors.xml` | Dark theme colors | ✅ READY |
| `res/values/accessibility_strings.xml` | 150+ content descriptions | ✅ READY |

### 2. Design Documentation
| Document | Purpose | Status |
|----------|---------|--------|
| `MODERN_DESIGN_AUDIT_PLAN.md` | Comprehensive design guide | ✅ READY |
| `ACCESSIBILITY_AUDIT_CHECKLIST.md` | Accessibility standards | ✅ READY |
| `UI_UX_MODERNIZATION_IMPLEMENTATION_GUIDE.md` | This file! | ✅ READY |

### 3. What's Next
Now we implement these across all screens systematically!

---

## Phase 1: Quick Wins (3-4 hours)

### Goal
Get core design system applied to critical screens and ensure build passes.

### Task List

#### 1.1 Update SignInActivity Layout ⏱️ 30 min
```xml
<!-- File: activity_sign_in.xml -->

<!-- CHANGES NEEDED: -->
- Replace button android:layout_height="40dp" with android:layout_height="@dimen/button_height_large" (48dp)
- Replace all padding with @dimen/padding_default (16dp)
- Add android:contentDescription to all ImageViews
- Replace hardcoded text colors with ?attr/colorOnBackground
- Add focus states to buttons
```

**Specific Changes**:
```xml
<!-- BEFORE -->
<Button
    android:layout_height="40dp"
    android:layout_marginStart="16dp"
    android:layout_marginEnd="16dp" />

<!-- AFTER -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.BookUp.Button.Filled"
    android:layout_height="@dimen/button_height_large" />
```

#### 1.2 Update SignInActivity Strings (Add Content Descriptions) ⏱️ 20 min
```xml
<!-- File: activity_sign_in.xml -->

<ImageView
    android:src="@drawable/ic_logo"
    android:contentDescription="@string/app_logo" />

<EditText
    android:inputType="textEmailAddress"
    android:contentDescription="@string/email_field"
    android:hint="@string/email_hint" />

<Button
    android:contentDescription="@string/login_button" />
```

#### 1.3 Update ProfileFragment Avatar ⏱️ 15 min
```xml
<!-- File: fragment_profile.xml -->

<ImageView
    style="@style/Widget.BookUp.Avatar"
    android:src="@drawable/ic_avatar"
    android:contentDescription="@string/user_avatar" />
```

#### 1.4 Update ChatActivity Send Button ⏱️ 10 min
```xml
<!-- File: activity_chat.xml -->

<!-- BEFORE: 32dp button -->
<ImageButton
    android:layout_width="32dp"
    android:layout_height="32dp"
    android:src="@drawable/ic_send" />

<!-- AFTER: 48dp button (Material standard) -->
<IconButton
    android:layout_width="@dimen/touch_target_min"
    android:layout_height="@dimen/touch_target_min"
    android:src="@drawable/ic_send"
    android:contentDescription="@string/send_message" />
```

#### 1.5 Build & Verify ⏱️ 10 min
```bash
./gradlew clean build
```

**Success Criteria**:
- ✅ Build passes with 0 errors
- ✅ No resource warnings
- ✅ All new dimens/styles used

---

## Phase 2: Color & Contrast (2-3 hours)

### Goal
Ensure all text meets WCAG AA standards (4.5:1 minimum).

### 2.1 Audit Tool Setup

**Option 1: WebAIM Checker (Online)**
1. Visit: https://webaim.org/resources/contrastchecker/
2. Enter foreground color
3. Enter background color
4. Check ratio

**Option 2: Android Accessibility Scanner**
```bash
# Install on device
adb install accessibility-scanner.apk

# Run app and use scanner
```

### 2.2 Quick Contrast Checks

| Text Type | Foreground | Background | Min Ratio | Current | Fix |
|-----------|-----------|-----------|----------|---------|-----|
| Button Text | #FFFFFF | #2E8B57 | 4.5:1 | ✅ | - |
| Body Text | #000000 | #FFFFFF | 4.5:1 | ✅ | - |
| Secondary Text | #666666 | #FFFFFF | 4.5:1 | ⏳ | Check |
| Error Text | #EF4444 | #FFFFFF | 4.5:1 | ⏳ | Check |
| Dark Mode Text | #E7E8EB | #0F1419 | 4.5:1 | ✅ | - |

**If Fails**: Update colors in `colors.xml` or `colors-night.xml`

### 2.3 Fix Dark Mode Issues
```xml
<!-- colors.xml (Light Mode) -->
<color name="text_secondary">#666666</color>

<!-- colors-night.xml (Dark Mode) -->
<color name="text_secondary">#A0A0A0</color>  <!-- Lighter for dark background -->
```

### 2.4 Test Dark Mode
```
1. Settings > Display > Dark theme > ON
2. Open app
3. Check all screens
4. If text hard to read, update colors-night.xml
```

---

## Phase 3: Dimensions & Spacing (2-3 hours)

### Goal
Ensure all UI elements use design system constants.

### 3.1 Replace Hardcoded Values

**Common Patterns to Replace**:

```xml
<!-- BEFORE: Hardcoded spacing -->
<LinearLayout
    android:padding="16dp"
    android:layout_margin="8dp">
    <Button
        android:layout_height="40dp"
        android:paddingStart="24dp"
        android:paddingEnd="24dp" />
</LinearLayout>

<!-- AFTER: Using design system -->
<LinearLayout
    android:padding="@dimen/padding_default"
    android:layout_margin="@dimen/margin_small">
    <Button
        style="@style/Widget.BookUp.Button.Filled"
        android:layout_height="@dimen/button_height_large" />
</LinearLayout>
```

### 3.2 Files to Update

| File | Focus | Estimated Time |
|------|-------|-----------------|
| `activity_sign_in.xml` | Buttons, fields, spacing | 15 min |
| `fragment_profile.xml` | Card padding, text sizing | 15 min |
| `activity_chat.xml` | List item spacing, avatar sizes | 20 min |
| `activity_tutor_details.xml` | Button heights, chip spacing | 20 min |
| `activity_booking_session.xml` | Field heights, button sizing | 15 min |
| `fragment_reviews_bottom_sheet.xml` | Rating bar size, button height | 10 min |

### 3.3 Specific Changes

#### activity_sign_in.xml
```xml
<!-- Replace these: -->
android:layout_height="40dp"           → android:layout_height="@dimen/button_height_large"
android:layout_marginStart="16dp"      → android:layout_marginStart="@dimen/margin_default"
android:paddingStart="24dp"            → android:paddingStart="@dimen/button_padding_horizontal"
android:layout_marginTop="8dp"         → android:layout_marginTop="@dimen/margin_small"
```

#### fragment_profile.xml
```xml
<!-- Replace these: -->
android:layout_width="56dp"            → android:layout_width="@dimen/avatar_size_default"
android:layout_height="56dp"           → android:layout_height="@dimen/avatar_size_default"
android:padding="12dp"                 → android:padding="@dimen/card_padding"
android:layout_height="40dp"           → android:layout_height="@dimen/button_height_large"
```

#### activity_chat.xml
```xml
<!-- Replace these: -->
android:layout_width="48dp"            → android:layout_width="@dimen/touch_target_min"
android:layout_height="48dp"           → android:layout_height="@dimen/touch_target_min"
android:layout_marginEnd="16dp"        → android:layout_marginEnd="@dimen/margin_default"
android:text_size="14sp"               → android:textSize="@dimen/text_size_body_medium"
```

---

## Phase 4: Text Styling & Typography (1-2 hours)

### Goal
Use consistent, accessible text styles throughout.

### 4.1 Common Text Style Usage

```xml
<!-- HEADLINES (Page titles) -->
<TextView
    android:text="Your Profile"
    style="@style/TextAppearance.BookUp.HeadlineLarge"
    android:textColor="?attr/colorOnBackground" />

<!-- BODY TEXT (Descriptions, messages) -->
<TextView
    android:text="Message content here"
    style="@style/TextAppearance.BookUp.BodyLarge"
    android:textColor="?attr/colorOnBackground" />

<!-- LABELS (Button text, field labels) -->
<Button
    android:text="Book Session"
    style="@style/TextAppearance.BookUp.LabelLarge" />

<!-- HELPER TEXT (Hints, captions) -->
<TextView
    android:text="Helper text for field"
    style="@style/TextAppearance.BookUp.Helper"
    android:textColor="?attr/colorOnSurfaceVariant" />

<!-- ERROR TEXT -->
<TextView
    android:text="Email is invalid"
    style="@style/TextAppearance.BookUp.Error" />
```

### 4.2 Update All TextViews

**Patterns to Replace**:
```xml
<!-- BEFORE: Hardcoded styling -->
<TextView
    android:textSize="16sp"
    android:textStyle="bold"
    android:textColor="@color/black" />

<!-- AFTER: Using style -->
<TextView
    style="@style/TextAppearance.BookUp.BodyLarge.Readable"
    android:textColor="?attr/colorOnBackground" />
```

---

## Phase 5: Component Styling (1-2 hours)

### Goal
Apply Material Design 3 component styles.

### 5.1 Button Styling

```xml
<!-- FILLED BUTTONS (Primary actions like "Book", "Save") -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.BookUp.Button.Filled"
    android:text="@string/book_session" />

<!-- OUTLINED BUTTONS (Secondary actions like "Cancel") -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.BookUp.Button.Outlined"
    android:text="@string/cancel" />

<!-- TEXT BUTTONS (Tertiary actions like "Learn more") -->
<com.google.android.material.button.MaterialButton
    style="@style/Widget.BookUp.Button.Text"
    android:text="@string/learn_more" />
```

### 5.2 Card Styling

```xml
<!-- MATERIAL CARD (All cards should use this) -->
<com.google.android.material.card.MaterialCardView
    style="@style/Widget.BookUp.Card"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <!-- Card content -->
</com.google.android.material.card.MaterialCardView>
```

### 5.3 Input Field Styling

```xml
<!-- TEXT INPUT (All form fields should use this) -->
<com.google.android.material.textfield.TextInputLayout
    style="@style/Widget.BookUp.TextInputLayout"
    android:layout_width="match_parent"
    android:layout_height="wrap_content">
    
    <EditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:contentDescription="@string/email_field"
        android:hint="@string/email_hint" />
</com.google.android.material.textfield.TextInputLayout>
```

---

## Phase 6: Accessibility Descriptions (1-2 hours)

### Goal
Add screen reader support by adding content descriptions.

### 6.1 All ImageViews Need Descriptions

```xml
<!-- DECORATIVE IMAGE (Hidden from screen reader) -->
<ImageView
    android:src="@drawable/decorative_image"
    android:contentDescription="@null"
    android:importantForAccessibility="no" />

<!-- MEANINGFUL IMAGE -->
<ImageView
    android:src="@drawable/ic_profile"
    android:contentDescription="@string/user_avatar"
    android:importantForAccessibility="yes" />
```

### 6.2 All Buttons Need Descriptions

```xml
<!-- IF BUTTON HAS TEXT, NO DESCRIPTION NEEDED -->
<Button
    android:text="Book Session" />  <!-- "Book Session" is announced -->

<!-- IF BUTTON HAS ONLY ICON, ADD DESCRIPTION -->
<ImageButton
    android:src="@drawable/ic_send"
    android:contentDescription="@string/send_message" />
```

### 6.3 Form Fields Need Labels

```xml
<!-- CORRECT WAY -->
<LinearLayout>
    <TextView
        android:id="@+id/email_label"
        android:text="@string/email_field"
        style="@style/TextAppearance.BookUp.LabelLarge" />
    
    <EditText
        android:id="@+id/email_input"
        android:labelFor="@+id/email_label"
        android:contentDescription="@string/email_field"
        android:hint="@string/email_hint" />
</LinearLayout>
```

### 6.4 Files to Update

| File | Action | Time |
|------|--------|------|
| `activity_sign_in.xml` | Add descriptions to all ImageViews and buttons | 15 min |
| `fragment_profile.xml` | Add avatar description, button descriptions | 15 min |
| `activity_chat.xml` | Add descriptions to send, attach, menu buttons | 20 min |
| `activity_tutor_details.xml` | Add descriptions to all action buttons | 15 min |
| `activity_booking_session.xml` | Add descriptions to calendar, clock icons | 10 min |

---

## Implementation Checklist

### ✅ Already Done
- [x] Created `res/values/dimens.xml` - 100+ constants
- [x] Created `res/values/styles.xml` - 25+ styles
- [x] Created `res/values-night/colors.xml` - Dark theme
- [x] Created `res/values/accessibility_strings.xml` - 150+ descriptions

### ⏳ To Do This Session

#### Phase 1: Quick Wins (30-45 min)
- [ ] Update button heights to 48dp in SignInActivity
- [ ] Add content descriptions to SignInActivity ImageViews
- [ ] Build and verify (should be quick)

#### Phase 2: Systematic Updates (2-3 hours)
- [ ] Apply @dimen/ constants to all layouts
- [ ] Replace hardcoded colors with ?attr/ references
- [ ] Apply text styles to all TextViews
- [ ] Apply component styles to buttons, cards, inputs

#### Phase 3: Accessibility (1-2 hours)
- [ ] Add content descriptions to all ImageViews
- [ ] Add form field labels and descriptions
- [ ] Test dark mode on all screens

#### Phase 4: Testing (30 min)
- [ ] Build clean build
- [ ] Visual inspection on multiple screen sizes
- [ ] Dark mode testing
- [ ] TalkBack screen reader testing

---

## Code Examples by File

### Example 1: Updated activity_sign_in.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="@dimen/padding_default"
    android:background="?attr/colorBackground">

    <!-- Logo -->
    <ImageView
        android:layout_width="@dimen/icon_size_extra_large"
        android:layout_height="@dimen/icon_size_extra_large"
        android:layout_gravity="center_horizontal"
        android:layout_marginBottom="@dimen/margin_large"
        android:src="@drawable/ic_logo"
        android:contentDescription="@string/app_logo" />

    <!-- Title -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/sign_in"
        style="@style/TextAppearance.BookUp.HeadlineLarge"
        android:textColor="?attr/colorOnBackground"
        android:layout_marginBottom="@dimen/margin_large" />

    <!-- Email Field -->
    <com.google.android.material.textfield.TextInputLayout
        style="@style/Widget.BookUp.TextInputLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/margin_default">

        <EditText
            android:id="@+id/email_input"
            android:layout_width="match_parent"
            android:layout_height="@dimen/text_input_height"
            android:inputType="textEmailAddress"
            android:contentDescription="@string/email_field"
            android:hint="@string/email_hint" />
    </com.google.android.material.textfield.TextInputLayout>

    <!-- Password Field -->
    <com.google.android.material.textfield.TextInputLayout
        style="@style/Widget.BookUp.TextInputLayout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginBottom="@dimen/margin_large"
        app:endIconMode="password_toggle">

        <EditText
            android:id="@+id/password_input"
            android:layout_width="match_parent"
            android:layout_height="@dimen/text_input_height"
            android:inputType="textPassword"
            android:contentDescription="@string/password_field"
            android:hint="@string/password_hint" />
    </com.google.android.material.textfield.TextInputLayout>

    <!-- Sign In Button -->
    <com.google.android.material.button.MaterialButton
        android:id="@+id/sign_in_button"
        style="@style/Widget.BookUp.Button.Filled"
        android:layout_width="match_parent"
        android:layout_height="@dimen/button_height_large"
        android:layout_marginBottom="@dimen/margin_default"
        android:text="@string/login_button"
        android:contentDescription="@string/login_button" />

    <!-- Sign Up Link -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:gravity="center"
        android:layout_marginTop="@dimen/margin_default"
        android:orientation="horizontal">

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/no_account"
            style="@style/TextAppearance.BookUp.BodyMedium"
            android:textColor="?attr/colorOnBackground" />

        <Button
            style="@style/Widget.BookUp.Button.Text"
            android:layout_width="wrap_content"
            android:layout_height="@dimen/touch_target_min"
            android:text="@string/sign_up_link"
            android:contentDescription="@string/sign_up_link" />
    </LinearLayout>

</LinearLayout>
```

### Example 2: Updated Fragment Profile Layout

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="?attr/colorBackground">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="@dimen/padding_default">

        <!-- Profile Card -->
        <com.google.android.material.card.MaterialCardView
            style="@style/Widget.BookUp.Card"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="@dimen/margin_large">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical"
                android:padding="@dimen/padding_default"
                android:gravity="center_horizontal">

                <!-- Avatar -->
                <ImageView
                    android:id="@+id/profile_avatar"
                    style="@style/Widget.BookUp.Avatar"
                    android:layout_marginBottom="@dimen/margin_default"
                    android:contentDescription="@string/user_avatar" />

                <!-- User Name -->
                <TextView
                    android:id="@+id/profile_name"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/TextAppearance.BookUp.HeadlineSmall"
                    android:textColor="?attr/colorOnBackground"
                    android:gravity="center"
                    android:layout_marginBottom="@dimen/margin_small" />

                <!-- User Email -->
                <TextView
                    android:id="@+id/profile_email"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/TextAppearance.BookUp.BodyMedium"
                    android:textColor="?attr/colorOnSurfaceVariant"
                    android:gravity="center"
                    android:layout_marginBottom="@dimen/margin_large" />

                <!-- Edit Button -->
                <com.google.android.material.button.MaterialButton
                    android:id="@+id/edit_profile_button"
                    style="@style/Widget.BookUp.Button.Filled"
                    android:layout_width="match_parent"
                    android:layout_height="@dimen/button_height_large"
                    android:text="@string/edit_profile"
                    android:contentDescription="@string/edit_profile" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

        <!-- Bio Section -->
        <com.google.android.material.card.MaterialCardView
            style="@style/Widget.BookUp.Card"
            android:layout_width="match_parent"
            android:layout_height="wrap_content">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="vertical">

                <TextView
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="@string/bio"
                    style="@style/TextAppearance.BookUp.TitleSmall"
                    android:textColor="?attr/colorOnBackground"
                    android:padding="@dimen/padding_default" />

                <TextView
                    android:id="@+id/profile_bio"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    style="@style/TextAppearance.BookUp.BodyMedium"
                    android:textColor="?attr/colorOnBackground"
                    android:padding="@dimen/padding_default"
                    android:contentDescription="@string/bio_field" />

            </LinearLayout>
        </com.google.android.material.card.MaterialCardView>

    </LinearLayout>
</ScrollView>
```

---

## Testing Checklist

### After Each Phase

#### Visual Quality
- [ ] No hardcoded dimensions visible
- [ ] All buttons are 48dp+ height
- [ ] All text readable
- [ ] All colors theme-aware (?attr/...)
- [ ] Consistent spacing throughout

#### Accessibility
- [ ] All images have content descriptions
- [ ] All form fields have labels
- [ ] Text size appropriate (14sp+ body)
- [ ] Color contrast sufficient (4.5:1)

#### Dark Mode
- [ ] No white text on light backgrounds
- [ ] All elements readable
- [ ] Theme switches automatically
- [ ] Colors look good

#### Build
- [ ] No compilation errors
- [ ] No resource warnings
- [ ] No lint errors (except known)

---

## Success Metrics

| Metric | Target | Measure |
|--------|--------|---------|
| **Spacing Consistency** | 95%+ | Audit 20 layouts, count dimension usage |
| **Color System Usage** | 100%+ | All colors use ?attr/ or @color/ |
| **Content Descriptions** | 100%+ | All images have descriptions |
| **Accessibility Compliance** | WCAG AA | Run accessibility scanner |
| **Dark Mode Coverage** | 100%+ | Test all screens in dark mode |
| **Contrast Ratio** | 4.5:1+ | Test all text pairs |
| **Touch Targets** | 48dp+ | Measure all buttons |
| **Build Status** | ✅ PASS | Clean build, 0 errors |

---

## Time Estimate

| Phase | Tasks | Time |
|-------|-------|------|
| Phase 1 | Design System Foundation | ✅ 0 min (DONE) |
| Phase 2 | Button & spacing updates | 45 min |
| Phase 3 | Color & contrast fixes | 30 min |
| Phase 4 | Typography & styles | 45 min |
| Phase 5 | Accessibility descriptions | 60 min |
| Phase 6 | Dark mode testing | 30 min |
| Phase 7 | Build & final QA | 30 min |
| **TOTAL** | **Complete Modernization** | **~3-4 hours** |

---

## Quick Reference: Common Replacements

### Spacing
```
16dp → @dimen/spacing_l or @dimen/padding_default
8dp  → @dimen/spacing_s or @dimen/margin_small
12dp → @dimen/spacing_m or @dimen/card_padding
24dp → @dimen/spacing_xl or @dimen/margin_large
```

### Button Heights
```
40dp → @dimen/button_height
48dp → @dimen/button_height_large
32dp → @dimen/button_height_small
```

### Font Sizes
```
24sp → @dimen/text_size_headline_small or @style/TextAppearance.BookUp.HeadlineSmall
16sp → @dimen/text_size_body_large or @style/TextAppearance.BookUp.BodyLarge
14sp → @dimen/text_size_body_medium or @style/TextAppearance.BookUp.BodyMedium
12sp → @dimen/text_size_body_small or @style/TextAppearance.BookUp.BodySmall
```

### Colors
```
@color/black → ?attr/colorOnBackground
@color/white → ?attr/colorBackground or ?attr/colorSurface
#000000 (hardcoded) → ?attr/colorOnBackground
#FFFFFF (hardcoded) → ?attr/colorSurface
```

---

## Next Steps

1. **Start Phase 2**: Pick SignInActivity as first test
   - Update all hardcoded dimensions to use dimens.xml
   - Update all hardcoded colors to use theme colors
   - Add content descriptions
   - Test build

2. **Roll Out Systematically**:
   - ProfileFragment → ChatActivity → TutorDetailsActivity
   - Each should be < 30 min once you get the pattern

3. **Test Continuously**:
   - After each file: Build and verify
   - Dark mode: Toggle settings
   - Accessibility: Run scanner

4. **Final Polish**:
   - Global QA pass on all screens
   - Device testing (multiple sizes)
   - Performance check

---

**Ready to start Phase 2? Let's modernize BookUp! 🎨**

