export function findFiles(ctx: any): string[] {
  const keys = ctx.keys();
  return keys.map(ctx);
}

export function findFilesWithNames(ctx: any): string[] {
  const keys = ctx.keys();
  const values = keys.map(ctx);
  return keys.reduce((o: any, k: string, i: number) => {
    const key = k.replace(/^\.\//g, '').replace(/\..+$/g, '');
    o[key] = values[i];
    return o;
  },                 {});

}

export function camelCaseToTitle(text: string): string {
  const result = text.replace(/([A-Z])/g, ' $1');
  return result.charAt(0).toUpperCase() + result.slice(1);
}

export function scrollTo(el: any): void {
  // inline: 'nearest' fixes an issue of the window moving horizontally when scrolling.
  el.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
}

export function widthOfRenderedText(text: string, className: string): number {
  const div = document.createElement('div');
  div.setAttribute('class', className);
  div.innerHTML = text;
  document.body.appendChild(div);
  const width = div.getBoundingClientRect().width;
  div.parentNode!.removeChild(div);
  return width;
}