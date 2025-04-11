export function getColor(username: string): string {
  let hash = 0;
  for (let i = 0; i < username.length; i++) {
    hash = username.charCodeAt(i) + ((hash << 5) - hash);
  }
  const r = (hash & 0xFF0000) >> 16;
  const g = (hash & 0x00FF00) >> 8;
  const b = hash & 0x0000FF;

  const brightness = 80;
  const lightR = Math.min(255, r + brightness);
  const lightG = Math.min(255, g + brightness);
  const lightB = Math.min(255, b + brightness);

  const toHex = (x: number) => {
    const hex = x.toString(16);
    return hex.length === 1 ? "0" + hex : hex;
  };

  const hexR = toHex(Math.round(lightR));
  const hexG = toHex(Math.round(lightG));
  const hexB = toHex(Math.round(lightB));

  return '#' + hexR + hexG + hexB;
}

export function getDate(date: any): string {
  const time = new Date(date);
    
  let am = false;
  let hours = time.getHours();
    
  if (hours >= 12 && hours <= 23) {
    am = false;
  } else {
    am = true;
  }

  if(hours > 12) {
    hours = hours % 12;
  }
  
  return `${getMonth(time.getMonth())} ${time.getDate()}, ${time.getFullYear()} ${hours}:${time.getMinutes()}${am? 'am':'pm'}`;
}

function getMonth(month: number) {
  const months: Record<number, string> = {
    1: 'Jan',
    2: 'Feb',
    3: 'Mar',
    4: 'Apr',
    5: 'May',
    6: 'Jun',
    7: 'Jul',
    8: 'Aug',
    9: 'Sep',
    10: 'Oct',
    11: 'Nov',
    12: 'Dec'
  };

  return months[month];
}