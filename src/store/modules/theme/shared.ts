import type { GlobalThemeOverrides } from 'naive-ui';
import { defu } from 'defu';
import { addColorAlpha, getColorPalette, getPaletteColorByNumber, getRgb } from '@sa/color';
import { DARK_CLASS } from '@/constants/app';
import { toggleHtmlClass } from '@/utils/common';
import { localStg } from '@/utils/storage';
import { overrideThemeSettings, themeSettings } from '@/theme/settings';
import { themeVars } from '@/theme/vars';

/** Init theme settings */
export function initThemeSettings() {
  const isProd = import.meta.env.PROD;

  // if it is development mode, the theme settings will not be cached, by update `themeSettings` in `src/theme/settings.ts` to update theme settings
  if (!isProd) return themeSettings;

  // if it is production mode, the theme settings will be cached in localStorage
  // if want to update theme settings when publish new version, please update `overrideThemeSettings` in `src/theme/settings.ts`

  const localSettings = localStg.get('themeSettings');

  let settings = defu(localSettings, themeSettings);

  const isOverride = localStg.get('overrideThemeFlag') === BUILD_TIME;

  if (!isOverride) {
    settings = defu(overrideThemeSettings, settings);

    localStg.set('overrideThemeFlag', BUILD_TIME);
  }

  return settings;
}

/**
 * create theme token css vars value by theme settings
 *
 * @param colors Theme colors
 * @param tokens Theme setting tokens
 * @param [recommended=false] Use recommended color. Default is `false`
 */
export function createThemeToken(
  colors: App.Theme.ThemeColor,
  tokens?: App.Theme.ThemeSetting['tokens'],
  recommended = false
) {
  const paletteColors = createThemePaletteColors(colors, recommended);

  const { light, dark } = tokens || themeSettings.tokens;

  const themeTokens: App.Theme.ThemeTokenCSSVars = {
    colors: {
      ...paletteColors,
      nprogress: paletteColors.primary,
      ...light.colors
    },
    boxShadow: {
      ...light.boxShadow
    }
  };

  const darkThemeTokens: App.Theme.ThemeTokenCSSVars = {
    colors: {
      ...themeTokens.colors,
      ...dark?.colors
    },
    boxShadow: {
      ...themeTokens.boxShadow,
      ...dark?.boxShadow
    }
  };

  return {
    themeTokens,
    darkThemeTokens
  };
}

/**
 * Create theme palette colors
 *
 * @param colors Theme colors
 * @param [recommended=false] Use recommended color. Default is `false`
 */
function createThemePaletteColors(colors: App.Theme.ThemeColor, recommended = false) {
  const colorKeys = Object.keys(colors) as App.Theme.ThemeColorKey[];
  const colorPaletteVar = {} as App.Theme.ThemePaletteColor;

  colorKeys.forEach(key => {
    const colorMap = getColorPalette(colors[key], recommended);

    colorPaletteVar[key] = colorMap.get(500)!;

    colorMap.forEach((hex, number) => {
      colorPaletteVar[`${key}-${number}`] = hex;
    });
  });

  return colorPaletteVar;
}

/**
 * Get css var by tokens
 *
 * @param tokens Theme base tokens
 */
function getCssVarByTokens(tokens: App.Theme.BaseToken) {
  const styles: string[] = [];

  function removeVarPrefix(value: string) {
    return value.replace('var(', '').replace(')', '');
  }

  function removeRgbPrefix(value: string) {
    return value.replace('rgb(', '').replace(')', '');
  }

  for (const [key, tokenValues] of Object.entries(themeVars)) {
    for (const [tokenKey, tokenValue] of Object.entries(tokenValues)) {
      let cssVarsKey = removeVarPrefix(tokenValue);
      let cssValue = tokens[key][tokenKey];

      if (key === 'colors') {
        cssVarsKey = removeRgbPrefix(cssVarsKey);
        const { r, g, b } = getRgb(cssValue);
        cssValue = `${r} ${g} ${b}`;
      }

      styles.push(`${cssVarsKey}: ${cssValue}`);
    }
  }

  const styleStr = styles.join(';');

  return styleStr;
}

/**
 * Add theme vars to global
 *
 * @param tokens
 */
export function addThemeVarsToGlobal(tokens: App.Theme.BaseToken, darkTokens: App.Theme.BaseToken) {
  const cssVarStr = getCssVarByTokens(tokens);
  const darkCssVarStr = getCssVarByTokens(darkTokens);

  // 深色主色（如政务蓝 #1E3A5F）无法通过算法生成正确的浅色变体
  // 强制覆盖为与政务蓝搭配的天蓝色系
  const primaryLightOverrides = `
    --primary-50-color: 227 239 250;
    --primary-100-color: 207 226 246;
    --primary-200-color: 179 210 236;
    --primary-300-color: 147 191 224;
    --primary-400-color: 101 163 206;
    --primary-color: 30 58 95;
    --primary-600-color: 22 45 75;
    --primary-700-color: 16 34 56;
    --primary-800-color: 10 22 37;
    --primary-900-color: 5 11 19;
    --primary-950-color: 2 5 9;
  `;

  const css = `
    :root {
      ${cssVarStr}
      ${primaryLightOverrides}
    }
  `;

  const darkCss = `
    html.${DARK_CLASS} {
      ${darkCssVarStr}
    }
  `;

  const styleId = 'theme-vars';

  const style = document.querySelector(`#${styleId}`) || document.createElement('style');

  style.id = styleId;

  style.textContent = css + darkCss;

  document.head.appendChild(style);
}

/**
 * Toggle css dark mode
 *
 * @param darkMode Is dark mode
 */
export function toggleCssDarkMode(darkMode = false) {
  const { add, remove } = toggleHtmlClass(DARK_CLASS);

  if (darkMode) {
    add();
  } else {
    remove();
  }
}

/**
 * Toggle auxiliary color modes
 *
 * @param grayscaleMode
 * @param colourWeakness
 */
export function toggleAuxiliaryColorModes(grayscaleMode = false, colourWeakness = false) {
  const htmlElement = document.documentElement;
  htmlElement.style.filter = [grayscaleMode ? 'grayscale(100%)' : '', colourWeakness ? 'invert(80%)' : '']
    .filter(Boolean)
    .join(' ');
}

type NaiveColorScene = '' | 'Suppl' | 'Hover' | 'Pressed' | 'Active';
type NaiveColorKey = `${App.Theme.ThemeColorKey}Color${NaiveColorScene}`;
type NaiveThemeColor = Partial<Record<NaiveColorKey, string>>;
interface NaiveColorAction {
  scene: NaiveColorScene;
  handler: (color: string) => string;
}

/**
 * Get naive theme colors
 *
 * @param colors Theme colors
 * @param [recommended=false] Use recommended color. Default is `false`
 */
function getNaiveThemeColors(colors: App.Theme.ThemeColor, recommended = false) {
  const colorActions: NaiveColorAction[] = [
    { scene: '', handler: color => color },
    { scene: 'Suppl', handler: color => color },
    { scene: 'Hover', handler: color => getPaletteColorByNumber(color, 500, recommended) },
    { scene: 'Pressed', handler: color => getPaletteColorByNumber(color, 700, recommended) },
    { scene: 'Active', handler: color => addColorAlpha(color, 0.1) }
  ];

  const themeColors: NaiveThemeColor = {};

  const colorEntries = Object.entries(colors) as [App.Theme.ThemeColorKey, string][];

  colorEntries.forEach(color => {
    colorActions.forEach(action => {
      const [colorType, colorValue] = color;
      const colorKey: NaiveColorKey = `${colorType}Color${action.scene}`;
      themeColors[colorKey] = action.handler(colorValue);
    });
  });

  return themeColors;
}

/**
 * Get naive theme
 *
 * @param colors Theme colors
 * @param settings Theme settings object
 * @param overrides Optional manual overrides from preset
 */
export function getNaiveTheme(
  colors: App.Theme.ThemeColor,
  settings: App.Theme.ThemeSetting,
  overrides?: GlobalThemeOverrides
) {
  const { primary: colorLoading } = colors;

  // 全局组件样式覆盖（政务蓝 + 统一圆角）
  const componentOverrides = {
    Button: {
      borderRadiusMedium: `${settings.themeRadius}px`,
      borderRadiusSmall: `${Math.max(settings.themeRadius - 2, 4)}px`,
      borderRadiusLarge: `${settings.themeRadius + 2}px`,
      // 强制使用政务蓝主色（避免 NaiveUI 自动生成色板偏移）
      colorPrimary: '#1E3A5F',
      colorHoverPrimary: '#2B5290',
      colorPressedPrimary: '#152B47',
      textColorPrimary: '#FFFFFF',
      textColorHoverPrimary: '#FFFFFF',
      textColorPressedPrimary: '#FFFFFF',
      borderPrimary: 'none',
      borderHoverPrimary: 'none',
      borderPressedPrimary: 'none',
      // Quaternary 按钮政务蓝文字色
      colorQuaternary: '#1E3A5F',
      colorHoverQuaternary: '#2B5290',
      colorPressedQuaternary: '#152B47',
      textColorQuaternary: '#1E3A5F',
      textColorHoverQuaternary: '#2B5290',
      textColorPressedQuaternary: '#152B47'
    },
    Card: {
      borderRadius: `${settings.themeRadius + 4}px`,
      paddingMedium: '20px',
      paddingLarge: '24px'
    },
    Input: { borderRadius: `${settings.themeRadius}px` },
    Select: { borderRadius: `${settings.themeRadius}px` },
    Tag: { borderRadius: '6px' },
    Alert: { borderRadiusMedium: '10px' },
    DataTable: { borderRadius: '12px' },
    Modal: { borderRadius: '14px' },
    Menu: {
      borderRadius: `${settings.themeRadius}px`,
      itemColorHover: 'rgba(30, 58, 95, 0.05)',
      itemColorActive: 'rgba(30, 58, 95, 0.10)',
      itemTextColorActive: '#1E3A5F',
      itemTextColorHover: '#1E3A5F',
      itemIconColorActive: '#1E3A5F',
      itemIconColorHover: '#1E3A5F'
    },
    LoadingBar: { colorLoading: colors.primary }
  } as GlobalThemeOverrides;

  const theme: GlobalThemeOverrides = {
    common: {
      ...getNaiveThemeColors(colors, settings.recommendColor),
      borderRadius: `${settings.themeRadius}px`,
      textColorBase: '#1E293B',
      textColor1: '#1E293B',
      textColor2: '#64748B',
      textColor3: '#94A3B8',
      textColorDisabled: '#94A3B8',
      borderColor: '#E2E8F0',
      dividerColor: '#E2E8F0',
      modalColor: '#FFFFFF',
      popoverColor: '#FFFFFF',
      tableColor: '#FFFFFF',
      cardColor: '#FFFFFF',
      bodyColor: '#F8FAFC',
      tagColor: '#F1F5F9'
    },
    ...componentOverrides
  };

  // If there are overrides, merge them with priority
  // overrides has higher priority than auto-generated theme
  return overrides ? defu(overrides, theme) : theme;
}
