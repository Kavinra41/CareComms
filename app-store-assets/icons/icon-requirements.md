# CareComms App Icon Requirements

## Design Guidelines

### Brand Colors
- Primary: Deep Purple (#6B46C1)
- Secondary: Light Purple (#A78BFA)
- Accent: White (#FFFFFF)
- Background: Clean white or subtle gradient

### Icon Concept
The CareComms icon should represent:
- Care and compassion (heart or hands)
- Communication (speech bubble or connection)
- Accessibility (clear, simple design)
- Professionalism (clean, medical aesthetic)

### Recommended Design Elements
1. **Primary Symbol**: Stylized heart with communication elements
2. **Secondary Elements**: Subtle connection lines or care symbols
3. **Typography**: Clean, readable "CC" monogram if text is included
4. **Style**: Modern, flat design with subtle depth

## iOS Icon Requirements

### Sizes Required
- 1024x1024 px (App Store)
- 180x180 px (iPhone 6 Plus, 6s Plus, 7 Plus, 8 Plus, X, XS, XS Max, 11 Pro Max)
- 167x167 px (iPad Pro)
- 152x152 px (iPad, iPad mini)
- 120x120 px (iPhone 6, 6s, 7, 8, X, XS, 11 Pro)
- 87x87 px (iPhone 6 Plus, 6s Plus, 7 Plus, 8 Plus, X, XS, XS Max, 11 Pro Max - Settings)
- 80x80 px (iPad - Settings)
- 76x76 px (iPad)
- 60x60 px (iPhone - Settings)
- 58x58 px (iPhone 6 Plus, 6s Plus, 7 Plus, 8 Plus, X, XS, XS Max, 11 Pro Max - Settings)
- 40x40 px (iPad - Settings)
- 29x29 px (iPhone, iPad - Settings)
- 20x20 px (iPhone, iPad - Notifications)

### iOS Design Guidelines
- No transparency or alpha channels
- Square format (iOS will apply corner radius)
- No text that will be illegible at small sizes
- Avoid using iOS interface elements
- Design for the full square - don't add your own corner radius

## Android Icon Requirements

### Sizes Required
- 512x512 px (Google Play Store)
- 192x192 px (XXXHDPI)
- 144x144 px (XXHDPI)
- 96x96 px (XHDPI)
- 72x72 px (HDPI)
- 48x48 px (MDPI)

### Android Adaptive Icon
- 108x108 dp canvas
- 72x72 dp safe zone for main content
- Foreground and background layers
- Support for various mask shapes

### Android Design Guidelines
- Use adaptive icon format for Android 8.0+
- Provide both foreground and background layers
- Ensure icon works with circular, square, and rounded square masks
- Test with different launcher themes

## Accessibility Requirements

### Visual Accessibility
- High contrast between elements
- Clear, recognizable shapes at all sizes
- No reliance on color alone to convey meaning
- Readable at minimum 16x16 pixels

### Color Accessibility
- Minimum 3:1 contrast ratio for graphics
- Consider colorblind users (avoid red/green combinations)
- Test in grayscale to ensure clarity

## File Formats and Specifications

### iOS
- Format: PNG (no transparency)
- Color Profile: sRGB
- Compression: Lossless

### Android
- Format: PNG with transparency support
- Color Profile: sRGB
- Compression: Optimized for file size

## Icon Variations

### Light Theme
- Deep purple primary elements
- White or light background
- High contrast for readability

### Dark Theme (if supported)
- Light purple or white elements
- Dark background
- Maintains brand recognition

## Testing Checklist

- [ ] Icon is recognizable at 16x16 pixels
- [ ] Works well on light and dark backgrounds
- [ ] Maintains brand consistency
- [ ] Passes accessibility contrast requirements
- [ ] Looks professional and trustworthy
- [ ] Appeals to target demographic (healthcare professionals and elderly users)
- [ ] Differentiates from competitors
- [ ] Works in grayscale
- [ ] No copyright or trademark issues

## Brand Guidelines Compliance

### Must Include
- CareComms brand colors
- Professional healthcare aesthetic
- Accessibility-focused design
- Clear, simple symbolism

### Must Avoid
- Complex details that don't scale
- Generic medical symbols (red cross, etc.)
- Overly technical or clinical appearance
- Colors that conflict with accessibility needs

## Delivery Requirements

### File Naming Convention
```
iOS:
- AppIcon-1024.png
- AppIcon-180.png
- AppIcon-167.png
- etc.

Android:
- ic_launcher-512.png
- ic_launcher_foreground.xml
- ic_launcher_background.xml
- mipmap-xxxhdpi/ic_launcher.png
- etc.
```

### Folder Structure
```
icons/
├── ios/
│   ├── AppIcon.appiconset/
│   └── individual-sizes/
├── android/
│   ├── adaptive/
│   └── legacy/
└── source/
    ├── master-icon.ai
    └── brand-guidelines.pdf
```