import type { DefineComponent } from 'vue';

declare global {
  namespace JSX {
    interface Element {}
    interface ElementClass extends DefineComponent {}
    interface IntrinsicElements {
      [elem: string]: any;
    }
  }
}
